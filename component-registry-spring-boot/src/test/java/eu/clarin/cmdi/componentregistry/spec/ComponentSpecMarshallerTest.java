/*
 * Copyright (C) 2024 CLARIN ERIC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.componentregistry.spec;

import eu.clarin.cmdi.componentregistry.rest.spec.ComponentSpecMarshaller;
import eu.clarin.cmdi.componentregistry.components.ComponentSpec;
import eu.clarin.cmdi.componentregistry.components.ObjectFactory;
import java.io.StringWriter;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj3.XmlAssert;
import org.xmlunit.builder.Input;

/**
 *
 * @author twagoo
 */
public class ComponentSpecMarshallerTest {

    /**
     * Test of marshall method, of class ComponentSpecMarshaller.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testMarshallEmpty() throws Exception {
        final ComponentSpecMarshaller instance = new ComponentSpecMarshaller();
        final ComponentSpec spec = new ObjectFactory().createComponentSpec();
        final StringWriter writer = new StringWriter();
        instance.marshall(spec, writer);

        final String marshalledXml = writer.toString();
        assertThat(marshalledXml).isNotBlank();

        //verify XML content
        final String expected
                = """
                  <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                  <ComponentSpec isProfile="false"/>
                  """;
        XmlAssert.assertThat(Input.fromString(marshalledXml))
                .and(Input.fromString(expected))
                .areIdentical();
    }

    /**
     * Test of unmarshall method, of class ComponentSpecMarshaller.
     * @throws java.lang.Exception
     */
    @Test
    public void testUnmarshallEmpty() throws Exception {
        System.out.println("unmarshall");
        String xml = """
                     <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                     <ComponentSpec isProfile="true"/>
                     """;
        ComponentSpecMarshaller instance = new ComponentSpecMarshaller();
        ComponentSpec spec = instance.unmarshall(xml);

        assertThat(spec).isNotNull();
        assertThat(spec).hasFieldOrPropertyWithValue("isProfile", true);
    }

}
