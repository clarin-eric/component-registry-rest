package clarin.cmdi.componentregistry.browser {
	import mx.controls.dataGridClasses.DataGridColumn;
	import mx.utils.ObjectUtil;

	public class BrowserColumns {
		public function BrowserColumns() {
		}

		public function getProfileColumns():Array {
			var columns:Array = new Array();
			columns.push(createColumn("name", "Name"));
			columns.push(createColumn("domainName", "Domain Name"));
			columns.push(createColumn("creatorName", "Creator"));
			columns.push(createColumn("description", "Description"));
			columns.push(createDateColumn());
			return columns;
		}

		public function getComponentColumns():Array {
			var columns:Array = new Array();
			columns.push(createColumn("name", "Name"));
			columns.push(createColumn("groupName", "Group Name"));
			columns.push(createColumn("domainName", "Domain Name"));
			columns.push(createColumn("creatorName", "Creator Name"));
			columns.push(createColumn("description", "Description"));
			columns.push(createDateColumn());
			return columns;
		}

		private function createColumn(dataField:String, headerText:String):DataGridColumn {
			var c:DataGridColumn = new DataGridColumn();
			c.dataField = dataField;
			c.headerText = headerText;
			c.dataTipField = dataField;
			c.sortCompareFunction = function(itemA:Object, itemB:Object):int { //Sort all string fields case insensitive
				return ObjectUtil.stringCompare(itemA[dataField], itemB[dataField], true);
			}
			return c;
		}

		private function createDateColumn():DataGridColumn {
			var c:DataGridColumn = createColumn("registrationDate", "Registration Date");
			c.sortCompareFunction = compareRegistrationDate;
			return c;

		}

		private function compareRegistrationDate(itemA:Object, itemB:Object):int {
			return ObjectUtil.dateCompare(itemA.registrationDateValue, itemB.registrationDateValue);
		}


	}
}