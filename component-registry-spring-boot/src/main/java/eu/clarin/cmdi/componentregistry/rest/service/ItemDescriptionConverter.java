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

import com.google.common.collect.Streams;
import eu.clarin.cmdi.componentregistry.rest.model.BaseDescription;
import eu.clarin.cmdi.componentregistry.rest.model.ComponentDescription;
import eu.clarin.cmdi.componentregistry.rest.model.ComponentsList;
import eu.clarin.cmdi.componentregistry.rest.model.ProfileDescription;
import eu.clarin.cmdi.componentregistry.rest.model.ProfilesList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 * @author twagoo
 */
@Component
public class ItemDescriptionConverter {

    public ComponentDescription baseDescriptionAsComponent(BaseDescription src) {
        final ComponentDescription target = new ComponentDescription();
        target.setCommentsCount(src.getCommentsCount());
        target.setCreatorName(src.getCreatorName());
        target.setDbId(src.getDbId());
        target.setDbUserId(src.getDbUserId());
        target.setDeleted(src.isDeleted());
        target.setDerivedfrom(src.getDerivedfrom());
        target.setDescription(src.getDescription());
        target.setDomainName(src.getDomainName());
        target.setGroupName(src.getGroupName());
        target.setHref(src.getHref());
        target.setId(src.getId());
        target.setName(src.getName());
        target.setPublic(src.isPublic());
        target.setRecommended(src.isRecommended());
        target.setRegistrationDate(src.getRegistrationDate());

        target.setShowInEditor(src.isShowInEditor());
        target.setStatus(src.getStatus());
        target.setSuccessor(src.getSuccessor());
        target.setUserId(src.getUserId());
        return target;
    }

    public ProfileDescription baseDescriptionAsProfile(BaseDescription src) {
        final ProfileDescription target = new ProfileDescription();
        target.setCommentsCount(src.getCommentsCount());
        target.setCreatorName(src.getCreatorName());
        target.setDbId(src.getDbId());
        target.setDbUserId(src.getDbUserId());
        target.setDeleted(src.isDeleted());
        target.setDerivedfrom(src.getDerivedfrom());
        target.setDescription(src.getDescription());
        target.setDomainName(src.getDomainName());
        target.setGroupName(src.getGroupName());
        target.setHref(src.getHref());
        target.setId(src.getId());
        target.setName(src.getName());
        target.setPublic(src.isPublic());
        target.setRecommended(src.isRecommended());
        target.setRegistrationDate(src.getRegistrationDate());
        target.setShowInEditor(src.isShowInEditor());
        target.setStatus(src.getStatus());
        target.setSuccessor(src.getSuccessor());
        target.setUserId(src.getUserId());
        if (src instanceof ProfileDescription pSrc) {
            target.setShowInEditor(pSrc.isShowInEditor());
        }
        return target;
    }

    public ComponentsList descriptionsAsComponentsList(Iterable<? extends BaseDescription> descriptions) {
        final List<ComponentDescription> componentDescriptions
                = Streams.stream(descriptions)
                        .map(this::baseDescriptionAsComponent)
                        .toList();

        return new ComponentsList(componentDescriptions);
    }

    public ProfilesList descriptionsAsProfilesList(Iterable<? extends BaseDescription> descriptions) {
        final List<ProfileDescription> profileDescriptions
                = Streams.stream(descriptions)
                        .map(this::baseDescriptionAsProfile)
                        .toList();

        return new ProfilesList(profileDescriptions);
    }

}
