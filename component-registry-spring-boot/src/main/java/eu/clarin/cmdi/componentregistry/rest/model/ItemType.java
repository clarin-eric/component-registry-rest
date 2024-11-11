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
package eu.clarin.cmdi.componentregistry.rest.model;

import jakarta.xml.bind.annotation.XmlEnumValue;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author twagoo
 */
public enum ItemType {

    @XmlEnumValue("component")
    COMPONENT,
    @XmlEnumValue("profile")
    PROFILE;

    public static class StringToItemTypeConverter implements Converter<String, ItemType> {

        @Override
        public ItemType convert(String source) {
            if (source == null) {
                return null;
            } else try {
                return ItemType.valueOf(source.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }
}
