/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.shared;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/** @author andrew00x */
public class PropertyFilter {
    /** Property filter for all properties. */
    public static final String ALL = "*";

    /** Property filter for skip all properties. */
    public static final String NONE = "none";

    /** Property filter for all properties. */
    public static final PropertyFilter ALL_FILTER;

    /** Property filter for skip all properties. */
    public static final PropertyFilter NONE_FILTER;

    static {
        ALL_FILTER = new PropertyFilter();
        ALL_FILTER.retrievalAllProperties = true;
        NONE_FILTER = new PropertyFilter();
        NONE_FILTER.propertyNames = Collections.emptySet();
    }

    /**
     * Construct new Property Filter.
     *
     * @param filterString
     *         the string that contains either '*' or comma-separated list of properties names. An arbitrary number of space allowed before
     *         and after each comma. If filterString is 'none' it minds all properties should be rejected by filter.
     * @return PropertyFilter instance
     * @throws IllegalArgumentException
     *         if {@code filterString} is invalid
     */
    public static PropertyFilter valueOf(String filterString) {
        if (filterString == null || filterString.length() == 0 || ALL.equals(filterString = filterString.trim())) {
            return ALL_FILTER;
        } else if (filterString.equalsIgnoreCase(NONE)) {
            return NONE_FILTER;
        }
        return new PropertyFilter(filterString);
    }

    /** Characters that split. */
    private static final Pattern SPLITTER = Pattern.compile("\\s*,\\s*");

    /** Characters that not allowed in property name. */
    private static final String ILLEGAL_CHARACTERS = ",\"'\\.()";

    /** Property names. */
    private Set<String> propertyNames;

    /** Is all properties requested. */
    private boolean retrievalAllProperties = false;

    /**
     * Construct new Property Filter.
     *
     * @param filterString
     *         the string that contains either '*' or comma-separated list of properties names. An arbitrary number of space allowed before
     *         and after each comma.
     * @throws IllegalArgumentException
     *         if {@code filterString} is invalid
     */
    private PropertyFilter(String filterString) {
        this.propertyNames = new HashSet<>();
        for (String token : SPLITTER.split(filterString)) {
            if (token.length() > 0 && !token.equals(ALL)) {
                for (char ch : token.toCharArray()) {
                    if (Character.isWhitespace(ch) || ILLEGAL_CHARACTERS.indexOf(ch) != -1) {
                        throw new IllegalArgumentException(String.format("Invalid filter '%s' contains illegal characters.", filterString));
                    }
                }
                this.propertyNames.add(token);
            } else {
                throw new IllegalArgumentException(
                        String.format("Invalid filter '%s'. Filter must contains either '*' OR comma-separated list of properties.",
                                      filterString));
            }
        }
    }

    private PropertyFilter() {
    }

    public boolean accept(String name) {
        return retrievalAllProperties || propertyNames.contains(name);
    }
}
