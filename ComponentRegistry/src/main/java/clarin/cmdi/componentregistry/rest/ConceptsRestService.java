/*
 * Copyright (C) 2025 CLARIN ERIC
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
package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.concepts.wikidata.WikiDataConceptsService;
import clarin.cmdi.componentregistry.model.Concept;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.springframework.stereotype.Service;

/**
 *
 * @author twagoo
 */
@Path("/concepts")
@Service
@Singleton
@Api(value = "/concepts", produces = MediaType.APPLICATION_JSON)
public class ConceptsRestService {

    public static final String ALL_TYPES = "all";

    @InjectParam
    private WikiDataConceptsService wdService;

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns a listing of groups to which an item belongs")
    public List<Concept> getGroupsTheItemIsAMemberOf(@QueryParam("q") String query, @QueryParam("type") @DefaultValue(ALL_TYPES) List<String> type) {
        return wdService.search(query, toWikidataTypes(type)).toList();
    }

    public String[] toWikidataTypes(List<String> types) {
        final ImmutableList.Builder<String> builder = ImmutableList.<String>builder();

        if (types != null) {
            //sanitise
            for (String type : types) {
                switch (type) {
                    case WikiDataConceptsService.ITEM_TYPE, WikiDataConceptsService.PROPERTY_TYPE ->
                        builder.add(type);
                    case ALL_TYPES -> {
                        builder.add(WikiDataConceptsService.ITEM_TYPE);
                        builder.add(WikiDataConceptsService.PROPERTY_TYPE);
                    }

                }
            }
        }
        return builder.build().toArray(String[]::new);
    }

}
