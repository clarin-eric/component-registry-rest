package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.browser.Browse;
	import clarin.cmdi.componentregistry.browser.SwitchViewEvent;
	import clarin.cmdi.componentregistry.common.Credentials;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.Login;
	import clarin.cmdi.componentregistry.editor.Editor;
	import clarin.cmdi.componentregistry.importer.Importer;
	import clarin.cmdi.componentregistry.services.Config;
	import clarin.cmdi.componentregistry.services.RegistrySpace;
	
	import flash.events.Event;
	
	import mx.containers.ViewStack;
	import mx.controls.Alert;
	import mx.events.CloseEvent;
	import mx.events.FlexEvent;
	
	public class RegistryViewStack extends ViewStack {
		public var browse:Browse = new Browse();
		private var editor:Editor = new Editor();
		private var importer:Importer = new Importer();
		
		private var loginPanel:Login;
		private var selectedItem:ItemDescription;
		
		public function RegistryViewStack() {
			loginPanel = new Login();
			loginPanel.addEventListener(Login.FAILED, loginFailed);
			browse.addEventListener(Browse.START_ITEM_LOADED, switchWithStartupItem);
			Config._instance.addEventListener(SwitchViewEvent.SWITCH_VIEW, onSwitchingView);
			addChild(browse); //everyone can browse
			
			if (!Config.instance.debug) {
				editor.addEventListener(FlexEvent.SHOW, checkLogin);
				importer.addEventListener(FlexEvent.SHOW, checkLogin);
			}
			addChild(editor);
			addChild(importer);
		}
		
		public static function showView(viewName:String, item:ItemDescription):void{
			var event:SwitchViewEvent  = new SwitchViewEvent(viewName, item);
			Config.instance.dispatchEvent(event);
		}
		
		private function onSwitchingView(event:SwitchViewEvent):void{
			switchView(event.view, event.item);
		}
		
		private function switchWithStartupItem(even:Event):void {
			var item:ItemDescription = browse.getSelectedStartItem();
			switchView(Config.instance.view, item);
		}
		
		public function loadStartup():void {
			if ((Config.instance.registrySpace.space == Config.SPACE_PRIVATE || Config.instance.registrySpace.space == Config.SPACE_GROUP) && !Credentials.instance.isLoggedIn()) {
				checkLogin();
			} else {
				if (Config.instance.startupItem) {
					browse.loadStartup();
				} else {
					switchView(Config.instance.view);
				}
			}
		}
		
		private function switchView(view:String, item:ItemDescription = null):void {
			if (view == Config.VIEW_BROWSE) {
				switchToBrowse(item);
			} else if (view == Config.VIEW_EDIT) {
				switchToEditor(item);
			} else if (view == Config.VIEW_IMPORT) {
				switchToImport();
			}
		}
		
		public function switchToBrowse(itemDescription:ItemDescription):void {
			if (itemDescription != null) {				
				
				//Alert.show(itemDescription.id + " " +itemDescription.isPrivate + " " + Config.instance.registrySpace.space + " "+Config.instance.registrySpace.groupId);
				
				if (itemDescription.isPrivate) {
					if (Config.instance.registrySpace.space == Config.SPACE_PUBLISHED) {
						// from public registry we can save only to a private space
					    Config.instance.registrySpace = new RegistrySpace(Config.SPACE_PRIVATE, "");
					} else {
						// registry, goup of private, stays the same
					}
				} else {					
					if (Config.instance.registrySpace.space != Config.SPACE_PUBLISHED) {
						// the item has been published, go to public space
						Config.instance.registrySpace = new RegistrySpace( Config.SPACE_PUBLISHED, "");
					} else {
						// public item in a public registry, the registry setting stays intact
					}
				}
				
				browse.refresh();
				browse.setSelectedDescription(itemDescription);
			} 
			this.selectedItem = itemDescription;
			this.selectedChild = browse;
		}
		
		public function switchToEditor(itemDescription:ItemDescription):void {
			doSwitchToEditor(itemDescription);
		}
		
		private function doSwitchToEditor(itemDescription:ItemDescription):void {
			this.selectedChild = editor;
			
			//memoise registry space of config
			//because component palette can alter it (unwanted side effect)
		   editor.registrySpaceEditor = new RegistrySpace(Config.instance.registrySpace.space, Config.instance.registrySpace.groupId);
		   
		   if(itemDescription != null) {
				this.selectedItem = itemDescription;
				editor.setDescription(itemDescription);
			}
		   }
		
		public function switchToImport():void {
			this.selectedChild = importer;
		}
		
		private function checkLogin(event:Event = null):void {
			if (!Credentials.instance.isLoggedIn()) {
				var itemId:String = Config.instance.startupItem;
				if (selectedItem) {
					itemId = selectedItem.id;
				}
				loginPanel.show(this, RegistryView(this.selectedChild).getType(), Config.instance.registrySpace.space, itemId);
			}
		}
		
		private function loginFailed(event:Event):void {
			this.selectedChild = browse;
		}
		
		public function getEditor():Editor {
			return this.editor;
		}
		
	}
}