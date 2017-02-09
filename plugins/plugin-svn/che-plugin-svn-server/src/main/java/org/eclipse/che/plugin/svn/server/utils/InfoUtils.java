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
package org.eclipse.che.plugin.svn.server.utils;

import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class InfoUtils {

    public static final String KEY_PATH = "Path";
    public static final String KEY_NAME = "Name";
    public static final String KEY_URL = "URL";
    public static final String KEY_RELATIVE_URL = "Relative URL";
    public static final String KEY_REPOSITORY_ROOT = "Repository Root";
    public static final String KEY_REPOSITORY_UUID = "Repository UUID";
    public static final String KEY_REVISION = "Revision";
    public static final String KEY_NODE_KIND = "Node Kind";
    public static final String KEY_SCHEDULE = "Schedule";
    public static final String KEY_LAST_CHANGE_AUTHOR = "Last Changed Author";
    public static final String KEY_LAST_CHANGED_REV = "Last Changed Rev";
    public static final String KEY_LAST_CHANGED_DATE = "Last Changed Date";

    /** The absolute path to the project on the server and must be hidden for the user. */
    public static final String KEY_WORKING_COPY_ROOT_PATH = "Working Copy Root Path";

    private static final String STARTSWITH_PATTERN = "^{0}: (.*)$";

    private InfoUtils() {
    }

    public static String getPath(final List<String> infoOutput) {
        final Pattern pattern = Pattern.compile(MessageFormat.format(STARTSWITH_PATTERN, KEY_PATH));
        return searchPattern(infoOutput, pattern);
    }

    private static String searchPattern(final List<String> infoOutput, final Pattern pattern) {
        for (String line: infoOutput) {
            final Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                final String value = matcher.group(1);
                return value;
            } else {
                continue;
            }
        }
        return null;
    }

    public static String getName(final List<String> infoOutput) {
        final Pattern pattern = Pattern.compile(MessageFormat.format(STARTSWITH_PATTERN, KEY_NAME));
        return searchPattern(infoOutput, pattern);
    }

    public static String getUrl(final List<String> infoOutput) {
        final Pattern pattern = Pattern.compile(MessageFormat.format(STARTSWITH_PATTERN, KEY_URL));
        return searchPattern(infoOutput, pattern);
    }

    public static String getRelativeUrl(final List<String> infoOutput) {
        final Pattern pattern = Pattern.compile(MessageFormat.format(STARTSWITH_PATTERN, KEY_RELATIVE_URL));
        return searchPattern(infoOutput, pattern);
    }

    public static String getRepositoryRoot(final List<String> infoOutput) {
        final Pattern pattern = Pattern.compile(MessageFormat.format(STARTSWITH_PATTERN, KEY_REPOSITORY_ROOT));
        return searchPattern(infoOutput, pattern);
    }

    public static String getRepositoryUUID(final List<String> infoOutput) {
        final Pattern pattern = Pattern.compile(MessageFormat.format(STARTSWITH_PATTERN, KEY_REPOSITORY_UUID));
        return searchPattern(infoOutput, pattern);
    }

    public static String getRevision(final List<String> infoOutput) {
        final Pattern pattern = Pattern.compile(MessageFormat.format(STARTSWITH_PATTERN, KEY_REVISION));
        return searchPattern(infoOutput, pattern);
    }

    public static String getNodeKind(final List<String> infoOutput) {
        final Pattern pattern = Pattern.compile(MessageFormat.format(STARTSWITH_PATTERN, KEY_NODE_KIND));
        return searchPattern(infoOutput, pattern);
    }

    public static String getSchedule(final List<String> infoOutput) {
        final Pattern pattern = Pattern.compile(MessageFormat.format(STARTSWITH_PATTERN, KEY_SCHEDULE));
        return searchPattern(infoOutput, pattern);
    }

    public static String getLastChangeAuthor(final List<String> infoOutput) {
        final Pattern pattern = Pattern.compile(MessageFormat.format(STARTSWITH_PATTERN, KEY_LAST_CHANGE_AUTHOR));
        return searchPattern(infoOutput, pattern);
    }

    public static String getLastChangedRev(final List<String> infoOutput) {
        final Pattern pattern = Pattern.compile(MessageFormat.format(STARTSWITH_PATTERN, KEY_LAST_CHANGED_REV));
        return searchPattern(infoOutput, pattern);
    }

    public static String getLastChangedDate(final List<String> infoOutput) {
        final Pattern pattern = Pattern.compile(MessageFormat.format(STARTSWITH_PATTERN, KEY_LAST_CHANGED_DATE));
        return searchPattern(infoOutput, pattern);
    }

}
