package clarin.cmdi.componentregistry.common {
	import clarin.cmdi.componentregistry.services.Config;

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
		public var isPrivate:Boolean;
		public var commentsCount:int;

		public function ItemDescription() {

		}

		private function create(itemDescription:XML, infoUrl:String, isProfileValue:Boolean, isPrivate:Boolean):void {
			this.id = itemDescription.id;
			this.name = itemDescription.name;			
			this.description = itemDescription.description;
			this.creatorName = itemDescription.creatorName;
			this.groupName = itemDescription.groupName;
			this.domainName = itemDescription.domainName;
			this.dataUrl = infoUrl + itemDescription.id;
			this.commentsCount = itemDescription.commentsCount;
			this.isProfile = isProfileValue;
			this.isPrivate = isPrivate;
			
			// getting ISO-8601 (GMT/UTC timezone) dates from the server. Need to convert this, flex does not support ISO-8601 times out of the box.
			this.registrationDateValue = DateUtils.convertDate(itemDescription.registrationDate);
			if(registrationDateValue){
				this.registrationDate = DateUtils.convertDateToString(registrationDateValue);
			}
		}

		public function createProfile(itemDescription:XML, isPrivate:Boolean):void {
			create(itemDescription, Config.instance.profileInfoUrl, true, isPrivate);
		}

		public function createComponent(itemDescription:XML, isPrivate:Boolean):void {
			create(itemDescription, Config.instance.componentInfoUrl, false, isPrivate);
		}
		
		public function toString():String{
			return (isProfile?"profile":"component")+" id:"+id+" name:"+name+" url:"+dataUrl;
		}

	}
}