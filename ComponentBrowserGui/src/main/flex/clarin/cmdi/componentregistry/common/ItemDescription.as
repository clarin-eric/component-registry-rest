package clarin.cmdi.componentregistry.common {
	import clarin.cmdi.componentregistry.services.Config;

	import mx.formatters.DateFormatter;

	[Bindable]
	public class ItemDescription {

		public var id:String;
		public var name:String;
		public var description:String;
		public var creatorName:String;
		public var groupName:String;
		public var domainName:String = "";
		public var dataUrl:String;
		public var isProfile:Boolean;
		public var registrationDate:String;
		public var registrationDateValue:Date;
		public var isInUserSpace:Boolean;

		public function ItemDescription() {

		}

		private function create(itemDescription:XML, infoUrl:String, isProfileValue:Boolean, isInUserSpace:Boolean):void {
			this.id = itemDescription.id;
			this.name = itemDescription.name;
			this.registrationDate = convertDate(itemDescription.registrationDate);
			this.description = itemDescription.description;
			this.creatorName = itemDescription.creatorName;
			this.groupName = itemDescription.groupName;
			this.domainName = itemDescription.domainName;
			this.dataUrl = infoUrl + itemDescription.id
			this.isProfile = isProfileValue;
			this.isInUserSpace = isInUserSpace;
		}

		/**
		 * getting ISO-8601 (GMT/UTC timezone) dates from the server. Need to convert this, flex does not support ISO-8601 times out of the box.
		 */
		private function convertDate(dateString:String):String {
			var validator:DateFormatter = new DateFormatter();
			var s:String = parseDate(dateString);
			var n:Number = Date.parse(s);
			this.registrationDateValue = new Date(n);
			var result:String;
			if (isNaN(n)) {
				trace("cannot convert date: " + dateString);
				result = dateString;
			} else {
				validator.formatString = "DD MMMM YYYY H:NN:SS"; //e.g. 02 December 2009 13:48:39 
				result = validator.format(registrationDateValue);
			}
			return result;
		}

		public static function parseDate(value:String):String {
			var dateStr:String = value;
			dateStr = dateStr.replace(/-/g, "/");
			dateStr = dateStr.replace("T", " ");
			dateStr = dateStr.replace("+00:00", " GMT-0000");
			return dateStr;
		}

		public function createProfile(itemDescription:XML, isInUserSpace:Boolean):void {
			create(itemDescription, Config.instance.profileInfoUrl, true, isInUserSpace);
		}

		public function createComponent(itemDescription:XML, isInUserSpace:Boolean):void {
			create(itemDescription, Config.instance.componentInfoUrl, false, isInUserSpace);
		}

	}
}