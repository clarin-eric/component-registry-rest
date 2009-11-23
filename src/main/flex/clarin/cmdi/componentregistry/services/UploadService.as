package clarin.cmdi.componentregistry.services {
	import clarin.cmdi.componentregistry.ItemDescription;
	import clarin.cmdi.componentregistry.events.UploadCompleteEvent;
	
	import flash.events.DataEvent;
	import flash.events.Event;
	import flash.events.HTTPStatusEvent;
	import flash.net.FileFilter;
	import flash.net.FileReference;
	import flash.net.URLRequest;
	import flash.net.URLRequestMethod;
	import flash.net.URLVariables;

	[Event(name="uploadComplete", type="clarin.cmdi.componentregistry.events.UploadCompleteEvent")]
	public class UploadService {

		public function UploadService() {
		}

		[Bindable]
		public var selectedFile:String = "";
		[Bindable]
		public var message:String = "";

		private var fileRef:FileReference = new FileReference();
		private var request:URLRequest = new URLRequest(Config.instance.getUrl(Config.UPLOAD_SERVICE));

		public function submitProfile(description:ItemDescription):void {
			try {
				request.method = URLRequestMethod.POST;
				var params:URLVariables = new URLVariables();
				params.creatorName = description.creatorName;
				params.description = description.description;
				params.name = description.name;
				request.data = params;
				fileRef.upload(request, "profileData");
			} catch (error:Error) {
				trace("Unable to upload file.");
			}
		}

		public function selectProfile(event:Event):void {
			fileRef.addEventListener(Event.SELECT, selectHandler);
			fileRef.addEventListener(HTTPStatusEvent.HTTP_STATUS, errorHandler);
			fileRef.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, responseHandler);
			var filter:FileFilter = new FileFilter("Xml Files (*.xml)", "*.xml");
			fileRef.browse(new Array(filter));
		}

		private function selectHandler(event:Event):void {
			selectedFile = fileRef.name;
			message = "";
		}

		private function errorHandler(event:HTTPStatusEvent):void {
			message = "Server Failed to handle registration. Unexpected error, try again later. (httpstatus code was: " + event.status + ")";
		}

		private function responseHandler(event:DataEvent):void {
			var response:XML = new XML(event.data);
			if (response.@registered == true) {
				var item:ItemDescription = new ItemDescription();
				item.create(response.profileDescription[0]);
				dispatchEvent(new UploadCompleteEvent(item));
			} else {
				createErrorMessage(response);
			}
		}

		private function createErrorMessage(response:XML):void {
			message = "Failed to register:";
			var errors:XMLList = response.errors.error;
			for each (var error:XML in errors) {
				message += " - " + error.toString() + "\n";
			}
		}
	}
}