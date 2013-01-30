package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.services.Config;

	import com.adobe.net.URI;

	import flash.events.MouseEvent;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;

	import mx.controls.Label;

	public class UserSettingsLabelButton extends LabelButton {

		[Bindable]
		public var viewType:String;
		[Bindable]
		public var spaceType:String;
		[Bindable]
		public var itemId:String;

		public function UserSettingsLabelButton() {
			super(handleClick, "settings");
			toolTip = "Click for user settings";
		}

		private function handleClick(event:MouseEvent):void {
			var req:URLRequest = new URLRequest(Config.instance.userSettingsUrl);
			navigateToURL(req, "_blank");
		}

	}
}