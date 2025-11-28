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
package clarin.cmdi.componentregistry.concepts.wikidata;

import clarin.cmdi.componentregistry.model.Concept;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class WikiDataConceptsServiceTest {

    private WikiDataConceptsService instance;

    @Before
    public void setUp() {
        instance = new WikiDataConceptsService();
        instance.init();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of search method, of class WikiDataConceptsService.
     */
    @Test
    public void testSearch() {
        final String query = "cat";

        final Stream<Concept> search = instance.search(query);
        assertFalse(search.findAny().isEmpty());
    }

}
