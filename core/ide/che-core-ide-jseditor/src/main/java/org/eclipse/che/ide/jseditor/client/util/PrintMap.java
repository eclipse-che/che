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
package org.eclipse.che.ide.jseditor.client.util;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility to pretty-print maps.
 *
 * @author "Mickaël Leduque"
 */
public final class PrintMap {

    private PrintMap() {
    }

    /**
     * Pretty prints a map, using toString() on both the keys and values.
     *
     * @param map
     *         the map to pretty print
     * @return the display of the map
     */
    public static <U, V> String printMap(final Map<U, V> map) {
        final ToStringConverter<U> keyConverter = new ToStringConverter<U>();
        final ToStringConverter<V> valueConverter = new ToStringConverter<V>();
        return printMap(map, keyConverter, valueConverter);
    }

    /**
     * Pretty prints a map, using toString() on both the values and the provided converter for the keys.
     *
     * @param map
     *         the map to pretty print
     * @param keyConverter
     *         the converter for the keys
     * @return the display of the map
     */
    public static <U, V> String printMap(final Map<U, V> map, final Converter<U> keyConverter) {
        final ToStringConverter<V> valueConverter = new ToStringConverter<V>();
        return printMap(map, keyConverter, valueConverter);
    }

    /**
     * Pretty prints a map, using the provided converters for the keys and values.
     *
     * @param map
     *         the map to pretty print
     * @param keyConverter
     *         the converter for the keys
     * @param valueConverter
     *         the converter for the values
     * @return the display of the map
     */
    public static <U, V> String printMap(final Map<U, V> map, final Converter<U> keyConverter, final Converter<V> valueConverter) {
        final StringBuilder sb = new StringBuilder("{ ");
        String separator = "";
        for (final Entry<U, V> entry : map.entrySet()) {
            sb.append(separator);
            sb.append(keyConverter.convert(entry.getKey()));
            sb.append("=>");
            sb.append(valueConverter.convert(entry.getValue()));
            separator = ", ";
        }
        sb.append(" }");
        return sb.toString();
    }

    /**
     * Interface for the object to string converters used to pretty print the maps.
     *
     * @param <U>
     *         the type of the objects to convert to String
     * @author "Mickaël Leduque"
     */
    public interface Converter<U> {
        String convert(U item);
    }

    /**
     * {@link Converter} that just uses {@link #toString()}.
     *
     * @param <U>
     * @author "Mickaël Leduque"
     */
    public static class ToStringConverter<U> implements Converter<U> {

        @Override
        public String convert(final U item) {
            return item.toString();
        }

    }
}
