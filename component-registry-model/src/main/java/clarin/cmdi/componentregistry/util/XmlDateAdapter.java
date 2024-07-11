package clarin.cmdi.componentregistry.util;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * Formats dates for XML (de)serialization into XML date time (with timezones) format
 * http://www.w3schools.com/schema/schema_dtypes_date.asp
 * 
 * Timezone is always GMT
 * 
 * @author george.georgovassilis@mpi.nl
 * 
 */
public class XmlDateAdapter extends XmlAdapter<String, Date> {

    @Override
    public String marshal(Date v) throws Exception {
	return DatesHelper.formatXmlDateTime(v);
    }

    @Override
    public Date unmarshal(String v) throws Exception {
	return DatesHelper.parseXmlDateTime(v);
    }

}