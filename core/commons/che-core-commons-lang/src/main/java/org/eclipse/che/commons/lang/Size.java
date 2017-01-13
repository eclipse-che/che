/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.commons.lang;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper for converting memory size to human readable representation and back, e.g 12K, 12kB, 1M, etc.
 *
 * @author andrew00x
 */
public class Size {

    /**
     * Convert memory to human readable representation, e.g. 1 kB
     *
     * @param sizeInBytes
     *         size in bytes
     * @return memory in human readable format
     * @throws java.lang.IllegalArgumentException
     *         if {@code sizeInBytes} is negative
     */
    public static String toHumanSize(long sizeInBytes) {
        if (sizeInBytes < 0) {
            throw new IllegalArgumentException(String.format("Negative size: %d", sizeInBytes));
        }
        if (sizeInBytes < K) {
            return String.format("%d B", sizeInBytes);
        }
        float size = 0.0f;
        String suffix = "PB";
        for (int i = 0, l = SIZE_UNITS.length; i < l; i++) {
            Pair<Long, String> sizeUnit = SIZE_UNITS[i];
            if (sizeInBytes >= sizeUnit.first) {
                size = (float)sizeInBytes / sizeUnit.first;
                suffix = sizeUnit.second;
                break;
            }
        }
        return String.format((size % 1.0f == 0) ? "%.0f %s" : "%.1f %s", size, suffix);
    }

    /**
     * Parse human readable size string to long size in bytes.
     *
     * @param humanSize
     *         human readable size string
     * @return long size in bytes
     * @throws IllegalArgumentException
     *         if {@code humanSize} has incorrect format
     */
    public static long parseSize(String humanSize) {
        return parseAndConvertToBytes(humanSize);
    }

    /**
     * Parse human readable size string to long size in megabytes.
     *
     * @param humanSize
     *         human readable size string
     * @return long size in megabytes
     * @throws IllegalArgumentException
     *         if {@code humanSize} has incorrect format
     */
    public static long parseSizeToMegabytes(String humanSize) {
        return parseAndConvertToBytes(humanSize) / M;
    }

    private static final long K = 1024;
    private static final long M = K * K;
    private static final long G = M * K;
    private static final long T = G * K;
    private static final long P = T * K;

    @SuppressWarnings("unchecked")
    private static Pair<Long, String>[] SIZE_UNITS = new Pair[]{
            Pair.of(P, "PB"),
            Pair.of(T, "TB"),
            Pair.of(G, "GB"),
            Pair.of(M, "MB"),
            Pair.of(K, "kB")
    };

    private static final Pattern HUMAN_SIZE_PATTERN = Pattern.compile("^([0-9]*(\\.[0-9]+)?)\\s*(\\S+)?$");

    private static long parseAndConvertToBytes(String sizeString) {
        final Matcher matcher;
        if ((matcher = HUMAN_SIZE_PATTERN.matcher(sizeString)).matches()) {
            final float size = Float.parseFloat(matcher.group(1));
            final String suffix = matcher.group(3);
            if (suffix == null) {
                return (long)size;
            }
            final String suffixL = suffix.toLowerCase(Locale.ENGLISH);
            switch (suffixL) {
                case "b":
                    return (long)(size);
                case "k":
                case "kb":
                case "kib":
                    return (long)(size * K);
                case "m":
                case "mb":
                case "mib":
                    return (long)(size * M);
                case "g":
                case "gb":
                case "gib":
                    return (long)(size * G);
                case "t":
                case "tb":
                case "tib":
                    return (long)(size * T);
                case "p":
                case "pb":
                case "pib":
                    return (long)(size * P);
            }
        }
        throw new IllegalArgumentException("Invalid size: " + sizeString);
    }

    private Size() {
    }
}
