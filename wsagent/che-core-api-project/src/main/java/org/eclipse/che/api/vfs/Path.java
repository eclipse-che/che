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
package org.eclipse.che.api.vfs;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Path of VirtualFile.
 *
 * @author andrew00x
 */
public final class Path {
    /** Create new path. */
    public static Path of(String path) {
        final String[] segments = splitToSegments(path);
        if (segments.length == 0) {
            return ROOT;
        }
        return new Path(path.charAt(0) == '/', normalizePathSegments(EMPTY_PATH, segments));
    }

    private static final String[] EMPTY_PATH    = new String[0];
    private static final Pattern  PATH_SPLITTER = Pattern.compile("/");

    public static final Path ROOT = new Path(true);

    private final    String[] elements;
    private final    boolean  absolute;
    private volatile int      hashCode;
    private volatile String   asString;

    private Path(boolean absolute, String... elements) {
        this.absolute = absolute;
        this.elements = elements;
    }

    public boolean isAbsolute() {
        return absolute;
    }

    public Path getParent() {
        return isRoot() ? null : elements.length == 1 ? ROOT : subPath(0, elements.length - 1);
    }

    public Path subPath(Path parent) {
        return subPath(parent.length(), elements.length);
    }

    public Path subPath(int beginIndex) {
        return subPath(beginIndex, elements.length);
    }

    public Path subPath(int beginIndex, int endIndex) {
        if (beginIndex < 0 || beginIndex >= elements.length || endIndex > elements.length || beginIndex >= endIndex) {
            throw new IllegalArgumentException("Invalid end or begin index. ");
        }
        final String[] subPathElements = Arrays.copyOfRange(elements, beginIndex, endIndex);
        return new Path(absolute && beginIndex == 0, subPathElements);
    }

    public String getName() {
        return isRoot() ? "" : element(elements.length - 1);
    }

    public String[] elements() {
        return Arrays.copyOf(elements, elements.length);
    }

    public int length() {
        return elements.length;
    }

    public String element(int index) {
        if (index < 0 || index >= elements.length) {
            throw new IllegalArgumentException("Invalid index. ");
        }
        return elements[index];
    }

    public boolean isRoot() {
        return absolute && elements.length == 0;
    }

    public boolean isChild(Path parent) {
        if (parent.elements.length >= this.elements.length) {
            return false;
        }
        for (int i = 0, parentLength = parent.elements.length; i < parentLength; i++) {
            if (!parent.elements[i].equals(this.elements[i])) {
                return false;
            }
        }
        return true;
    }

    public Path newPath(String relative) {
        return newPath(splitToSegments(relative));
    }

    private static String[] splitToSegments(String rawPath) {
        return (isNullOrEmpty(rawPath) || ((rawPath.length() == 1) && (rawPath.charAt(0) == '/')))
               ? EMPTY_PATH : PATH_SPLITTER.split(rawPath.charAt(0) == '/' ? rawPath.substring(1) : rawPath);
    }

    public Path newPath(String... relative) {
        if (relative.length == 0) {
            return this;
        }
        return new Path(absolute, normalizePathSegments(elements, relative));
    }

    private static String[] normalizePathSegments(String[] parent, String[] relative) {
        List<String> segmentsList = new ArrayList<>(parent.length + relative.length);
        Collections.addAll(segmentsList, parent);
        for (String segment : relative) {
            if ("..".equals(segment)) {
                int size = segmentsList.size();
                if (size == 0) {
                    throw new IllegalArgumentException(String.format("Invalid path '%s', '..' on root. ", Joiner.on('/').join(relative)));
                }
                segmentsList.remove(size - 1);
            } else if (!(".".equals(segment))) {
                segmentsList.add(segment);
            }
        }
        if (segmentsList.isEmpty()) {
            return EMPTY_PATH;
        }
        return segmentsList.toArray(new String[segmentsList.size()]);
    }

    public Path newPath(Path relative) {
        final String[] newPath = new String[elements.length + relative.elements.length];
        System.arraycopy(elements, 0, newPath, 0, elements.length);
        System.arraycopy(relative.elements, 0, newPath, elements.length, relative.elements.length);
        return new Path(absolute, newPath);
    }

    public String join(char separator) {
        StringBuilder builder = new StringBuilder();
        if (absolute) {
            builder.append(separator);
        }
        Joiner.on(separator).appendTo(builder, elements);
        return builder.toString();
    }

   /* ==================================================== */

    @Override
    public String toString() {
        if (asString == null) {
            if (isRoot()) {
                asString = "/";
            } else {
                asString = join('/');
            }
        }
        return asString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Path) {
            Path path = (Path)o;
            return Arrays.equals(elements, path.elements);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Arrays.hashCode(elements);
        }
        return hashCode;
    }
}
