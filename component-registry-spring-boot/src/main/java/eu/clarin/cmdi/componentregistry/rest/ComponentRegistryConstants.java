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
package eu.clarin.cmdi.componentregistry.rest;

/**
 *
 * @author twagoo
 */
public class ComponentRegistryConstants {

    public static final String REGISTRY_ID_PREFIX = "clarin.eu:cr1:";

    // Attention! COMPONENT_PREFIX here and the client's Config.COMPONENT_PREFIX must be the same 
    // If you change COMPONENT_PREFIX here, then change the client's  config.COMPONENT_PREFIX
    public static final String COMPONENT_ID_PREFIX = REGISTRY_ID_PREFIX + "c_";

    // Attention! PROFILE_PREFIX here and the client's Config.PROFILE_PREFIX must be the same 
    // If you change PROFILE_PREFIX here, then the client's  Config.PROFILE_PREFIX
    public static final String PROFILE_ID_PREFIX = REGISTRY_ID_PREFIX + "p_";
}
