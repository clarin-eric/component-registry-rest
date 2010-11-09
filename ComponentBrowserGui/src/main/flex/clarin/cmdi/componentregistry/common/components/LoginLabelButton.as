package clarin.cmdi.componentregistry.common.components {
	import clarin.cmdi.componentregistry.services.Config;

	import com.adobe.net.URI;

	import flash.events.MouseEvent;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;

	import mx.controls.Label;

	public class LoginLabelButton extends Label {

		[Bindable]
		public var viewType:String;
		[Bindable]
		public var spaceType:String;
		[Bindable]
		public var itemId:String;

		public function LoginLabelButton() {
			super();
			text = "login";
			toolTip = "click to login";
			setStyle("color", "green");
			setStyle("textDecoration", "underline")
			addEventListener(MouseEvent.CLICK, handleLogin);
		}

		private function handleLogin(event:MouseEvent):void {
			var req:URLRequest = new URLRequest();

			var uri:URI = new URI(Config.instance.serviceRootUrl);
			uri.setQueryValue("shhaaDo", "lI");
			if (viewType) {
				uri.setQueryValue("view", viewType);
			}
			if (spaceType) {
				uri.setQueryValue("space", spaceType);
			}
			if (itemId) {
				uri.setQueryValue("item", itemId);
			}
			req.url = uri.toString();
			navigateToURL(req, "_top");
		}

	}
}