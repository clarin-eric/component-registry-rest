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
package eu.clarin.cmdi.componentregistry.rest.service;

import eu.clarin.cmdi.componentregistry.components.ComponentSpec;
import eu.clarin.cmdi.componentregistry.rest.model.BaseDescription;
import eu.clarin.cmdi.componentregistry.rest.persistence.RegistryItemRepository;
import eu.clarin.cmdi.componentregistry.rest.spec.ComponentSpecMarshaller;
import jakarta.xml.bind.JAXBException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import eu.clarin.cmdi.componentregistry.rest.persistence.SpecRepository;

/**
 *
 * @author twagoo
 */
@Service
public class ComponentRegistryServiceImpl implements ComponentRegistryService {

    private final RegistryItemRepository itemRepository;
    private final SpecRepository specRepository;
    private final ComponentSpecMarshaller specMarshaller;

    @Autowired
    public ComponentRegistryServiceImpl(
            RegistryItemRepository itemRepository,
            SpecRepository contentRepository,
            ComponentSpecMarshaller specMarshaller) {
        this.itemRepository = itemRepository;
        this.specRepository = contentRepository;
        this.specMarshaller = specMarshaller;
    }

    @Override
    public List<BaseDescription> getPublishedDescriptions() {
        return itemRepository.findPublicItems(Sort.unsorted());
    }

    @Override
    public List<BaseDescription> getPublishedDescriptions(String sortBy, Direction sortDirection) {
        return itemRepository.findPublicItems(Sort.by(sortDirection, sortBy));
    }

    @Override
    public BaseDescription getItemDescription(String componentId) {
        return itemRepository.findByComponentId(componentId);
    }

    @Override
    public ComponentSpec getItemSpecification(String componentId) {
        final String xml = specRepository.getSpecByComponentId(componentId);
        if (xml == null) {
            return null;
        } else {
            try {
                return specMarshaller.unmarshall(xml);
            } catch (JAXBException ex) {
                throw new RuntimeException("", ex);
            }
        }
    }

    @Override
    public String getItemSpecificationXml(String componentId) {
        return specRepository.getSpecByComponentId(componentId);
    }

}
