package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.browser.CMDComponentXMLBrowser;
	import clarin.cmdi.componentregistry.common.CMDSpecRenderer;
	import clarin.cmdi.componentregistry.common.Component;
	import clarin.cmdi.componentregistry.common.ItemDescription;
	import clarin.cmdi.componentregistry.common.StyleConstants;
	import clarin.cmdi.componentregistry.editor.CMDComponentXMLEditor;
	import clarin.cmdi.componentregistry.editor.model.CMDModelFactory;
	import clarin.cmdi.componentregistry.services.ComponentInfoService;
	import clarin.cmdi.componentregistry.services.ComponentListService;
	import clarin.cmdi.componentregistry.services.Config;
	
	import flash.display.DisplayObject;
	import flash.events.MouseEvent;
	
	import mx.containers.VBox;
	import mx.controls.Alert;
	import mx.controls.Label;
	import mx.managers.CursorManager;

	public class ExpandingComponentLabel extends VBox {

		[Bindable]
		public var isExpanded:Boolean = false;
		private var expandBusy:Boolean = false;

		private var expanded:DisplayObject;
		private var componentId:String;
		private var item:ItemDescription;
		private var componentSrv:ComponentInfoService = new ComponentInfoService();

		private var editable:Boolean;
		
		protected function setItem(item:ItemDescription):void{
			this.item = item;
		}

		public function ExpandingComponentLabel(componentId:String, editable:Boolean = false) {
			super();
			this.editable = editable;
			this.componentId = componentId;			
			this.item = Config.instance.getComponentsSrv(Config.SPACE_USER).findDescription(componentId);
			if (this.item==null) {
				this.item = Config.instance.getComponentsSrv(Config.SPACE_PUBLIC).findDescription(componentId);
			}
			styleName = StyleConstants.EXPANDING_COMPONENT;
			if (item && item.space == Config.SPACE_USER) {
				this.setStyle("borderColor", StyleConstants.USER_BORDER_COLOR);
			}
			if (item!=null)
				updateView();
			//unfortunately the componend and profile services may overlook components from groups, so we have to ask the backend
			else{
				Config.instance.getComponentsSrv(Config.SPACE_USER).getComponent(componentId, function(item:ItemDescription):void{
					setStyle("borderColor", StyleConstants.GROUP_BORDER_COLOR);
					setItem(item);
					updateView();
				});
			}
		}
		
		private function updateView():void{
			var id:Label = new Label();
			if (item) {
				id.text = item.name;
				id.styleName = StyleConstants.XMLBROWSER_HEADER;
				id.addEventListener(MouseEvent.CLICK, handleClick);
				id.addEventListener(MouseEvent.MOUSE_OVER, mouseOver);
				id.addEventListener(MouseEvent.MOUSE_OUT, mouseOut);
			} else {
				id.text = "Component cannot be found (might not exist anymore).";
			}
			addChild(id);
		}

		private function handleClick(event:MouseEvent):void {
			if (!expandBusy) {
				expandStart();
				try {
					if (isExpanded) {
						isExpanded = false;
						unexpand();
					} else {
						isExpanded = true;
						expand();
					}
				} catch (err:Error) {
					trace(err);
					CursorManager.removeBusyCursor();
				}
			}
		}

		private function expandStart():void {
			expandBusy = true;
			CursorManager.setBusyCursor();
		}

		private function expandFinished():void {
			expandBusy = false;
			CursorManager.removeBusyCursor();
		}


		private function unexpand():void {
			if (expanded != null) {
				removeChild(expanded);
				expanded = null;
			}
			expandFinished();
		}

		private function expand():void {
			componentSrv.addEventListener(ComponentInfoService.COMPONENT_LOADED, handleComponentLoaded);
			componentSrv.load(item);
		}

		private function handleComponentLoaded(event:Event):void {
			trace("ExpandingComponentLable's handleComponentLoaded is called, it is calling CMDComponentLabel");
			var comp:Component = componentSrv.component;
			if (editable) {
				expanded = new CMDComponentXMLEditor();
			} else {
				expanded = new CMDComponentXMLBrowser();
			}
			(expanded as CMDSpecRenderer).cmdSpec = CMDModelFactory.createModel(comp.componentMD.xml, comp.description);
			addChild(expanded);
			expandFinished();
		}


		private function mouseOver(event:MouseEvent):void {
			event.currentTarget.setStyle("color", "0x0000FF");
			event.currentTarget.setStyle("textDecoration", "underline");
		}

		private function mouseOut(event:MouseEvent):void {
			event.currentTarget.setStyle("color", "0x000000");
			event.currentTarget.setStyle("textDecoration", "none");
		}

	}
}