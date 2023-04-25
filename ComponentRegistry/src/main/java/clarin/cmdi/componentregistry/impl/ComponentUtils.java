package clarin.cmdi.componentregistry.impl;

import clarin.cmdi.componentregistry.ComponentRegistryException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;

import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

/**
 * Utilities for working with {@link BaseDescription}s
 *
 * @author george.georgovassilis@mpi.nl
 *
 */
public class ComponentUtils {

    public static boolean isProfileId(String componentId) {
        return componentId != null && componentId.startsWith(ProfileDescription.PROFILE_PREFIX);
    }

    public static boolean isComponentId(String componentId) {
        return componentId != null && componentId.startsWith(ComponentDescription.COMPONENT_PREFIX);
    }

    public static void copyPropertiesFrom(BaseDescription from, BaseDescription to) {
        BeanUtils.copyProperties(from, to);
    }

    public static Date getDate(String registrationDate) throws ParseException {
        return DateUtils.parseDate(registrationDate,
                new String[]{DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT
                            .getPattern()});
    }

    public static String createPublicHref(String href) {
        String result = href;
        if (href != null) {
            int index = href.indexOf("?");
            if (index != -1) { // strip off query params the rest should be the
                // public href.
                result = href.substring(0, index);
            }
        }
        return result;
    }
    /**
     * Compares two descriptions by the their value as returned by null null
     * null null     {@link BaseDescription#getName()
     * }
     */
    public static final Comparator<? super BaseDescription> COMPARE_ON_NAME = new Comparator<BaseDescription>() {
        @Override
        public int compare(BaseDescription o1, BaseDescription o2) {
            int result = 0;
            if (o1.getName() != null && o2.getName() != null) {
                result = o1.getName().compareToIgnoreCase(o2.getName());
            }
            if (o1.getId() != null && result == 0) {
                result = o1.getId().compareTo(o2.getId());
            }
            return result;
        }
    };

    public static final Comparator<? super Date> COMPARE_ON_DATE = new Comparator<Date>() {
        /**
         * @returns 1 if o11 is older than o2, returns -1 if o1 is younger than
         * o2
         */
        @Override
        public int compare(Date o1, Date o2) {
            // we need to sort not in standard ascending orde, but in descending, from higher (later date on the top) to the smaller (older date on the bottm)
            return (-o1.compareTo(o2));
        }
    };

    public static ProfileDescription toProfile(BaseDescription baseDescription) throws ComponentRegistryException {
        if (baseDescription == null) {
            return null;
        }
        if (isProfileId(baseDescription.getId())) {
            ProfileDescription copy = new ProfileDescription();
            BeanUtils.copyProperties(baseDescription, copy);
            return copy;
        } else {
            throw new ComponentRegistryException("The item is not a profile.");
        }
    }

    public static ComponentDescription toComponent(BaseDescription baseDescription) throws ComponentRegistryException {
        if (baseDescription == null) {
            return null;
        }
        if (isComponentId(baseDescription.getId())) {
            ComponentDescription copy = new ComponentDescription();
            BeanUtils.copyProperties(baseDescription, copy);
            return copy;
        } else {
            throw new ComponentRegistryException("The item is not a component");
        }
    }

    /**
     * "Upcast" a description to either {@link ProfileDescription} or
     * {@link ComponentDescription} depending on the identifier in the provided
     * description
     *
     * @param newDescr description to convert
     * @return converted description, either {@link ProfileDescription} or
     * {@link ComponentDescription}
     * @throws ComponentRegistryException
     */
    public static BaseDescription toTypeByIdPrefix(final BaseDescription newDescr) throws ComponentRegistryException {
        return isProfileId(newDescr.getId()) ? toProfile(newDescr) : toComponent(newDescr);
    }

    public static List<ProfileDescription> toProfiles(
            List<BaseDescription> baseDescription) {
        if (baseDescription == null) {
            return null;
        }
        List<ProfileDescription> list = new ArrayList<ProfileDescription>();
        for (BaseDescription c : baseDescription) {
            try {
                list.add(toProfile(c));
            } catch (ComponentRegistryException e) {
            }
        }
        return list;
    }

    public static List<ComponentDescription> toComponents(
            List<BaseDescription> baseDescription) {
        if (baseDescription == null) {
            return null;
        }
        List<ComponentDescription> list = new ArrayList<ComponentDescription>();
        for (BaseDescription c : baseDescription) {
            try {
                list.add(toComponent(c));
            } catch (ComponentRegistryException e) {
            }
        }
        return list;
    }
}
