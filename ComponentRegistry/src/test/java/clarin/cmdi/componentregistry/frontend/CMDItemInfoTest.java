package clarin.cmdi.componentregistry.frontend;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.RegistrySpace;

import org.junit.Test;

import clarin.cmdi.componentregistry.BaseDescription;
import clarin.cmdi.componentregistry.ProfileDescription;

public class CMDItemInfoTest extends BaseUnitTest{

    // this is component status "private/public" from the database v.s. registry space public/private
    @Test
    public void testGetUserDir() throws Exception {
	CMDItemInfo info = new CMDItemInfo(marshaller);
	//info.setDataNode(new FileNode(createFile("users/user1/components/c_123/description.xml"), false));
	info.setDataNode(new DisplayDataNode("test", true, false, createDescription(), RegistrySpace.PRIVATE, null));
	assertTrue(info.isDeletable());
	assertFalse(info.isUndeletable());
	assertTrue(info.isEditable());
	assertFalse(info.getDataNode().isDeleted());
	assertEquals(RegistrySpace.PRIVATE, info.getSpace());
	//info.setDataNode(new FileNode(createFile("users/user1/components/deleted/c_123/description.xml"), true));
	info.setDataNode(new DisplayDataNode("test", true, true, createDescription(), RegistrySpace.PRIVATE, null));
	assertFalse(info.isDeletable());
	assertTrue(info.isUndeletable());
	assertTrue(info.isEditable());
	assertTrue(info.getDataNode().isDeleted());
	assertEquals(RegistrySpace.PRIVATE, info.getSpace());
	//info.setDataNode(new FileNode(createFile("components/c_123/description.xml"), false));
	info.setDataNode(new DisplayDataNode("test", true, false, createDescription(), RegistrySpace.PUBLISHED, null));
	assertTrue(info.isDeletable());
	assertFalse(info.isUndeletable());
	assertTrue(info.isEditable());
	assertEquals(RegistrySpace.PUBLISHED, info.getSpace());
	//info.setDataNode(new FileNode(createFile("components/c_123/"), false));
	info.setDataNode(new DisplayDataNode("test", false, null));
	assertFalse(info.isDeletable());
	assertFalse(info.isUndeletable());
	assertFalse(info.isEditable());
	//info.setDataNode(new FileNode(createFile("components/deleted/c_123/description.xml"), true));
	info.setDataNode(new DisplayDataNode("test", true, true, createDescription(), RegistrySpace.PUBLISHED, null));
	assertFalse(info.isDeletable());
	assertTrue(info.isUndeletable());
	assertTrue(info.isEditable());
	assertEquals(RegistrySpace.PUBLISHED, info.getSpace());
	assertTrue(info.getDescription().startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<profileDescription"));
	//info.setDataNode(new FileNode(createFile("components/deleted/c_123/"), true));
	info.setDataNode(new DisplayDataNode("test", true, null));
	assertFalse(info.isDeletable());
	assertFalse(info.isUndeletable());
	assertFalse(info.isEditable());
	assertEquals("", info.getDescription());
    }

    private BaseDescription createDescription() {
	return ProfileDescription.createNewDescription();
    }
}
