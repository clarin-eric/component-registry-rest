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
package eu.clarin.cmdi.componentregistry.jersey.spec;

import eu.clarin.cmdi.componentregistry.components.ComponentSpec;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.StringReader;
import java.io.Writer;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.DefaultHandler2;

/**
 *
 * @author twagoo
 */
@Component
public class ComponentSpecMarshaller {

    private final JAXBContext context;

    public ComponentSpecMarshaller() throws JAXBException {
        this(JAXBContext.newInstance(ComponentSpec.class));
    }

    public ComponentSpecMarshaller(JAXBContext context) {
        this.context = context;
    }

    public void marshall(ComponentSpec spec, Writer writer) throws JAXBException {
        final Marshaller marshaller = context.createMarshaller();
        marshaller.marshal(spec, writer);
    }

    public ComponentSpec unmarshall(String xml) throws JAXBException {
        final StringReader reader = new StringReader(xml);
        final Object spec = context.createUnmarshaller().unmarshal(reader);
        assert (spec instanceof ComponentSpec);
        return (ComponentSpec) spec;
    }

}
