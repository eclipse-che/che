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

import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Subversion utilities.
 */
public class SubversionUtils {

    private static Pattern AT_REV_NUM_PATTERN = Pattern.compile("^At revision ([0-9]+).*");
    private static Pattern CHECKOUT_REV_NUM_PATTERN = Pattern.compile("^Checked out revision ([0-9]+).*");
    private static Pattern COMMIT_REV_NUM_PATTERN = Pattern.compile("^Committed revision ([0-9]+).*");
    private static Pattern UPDATE_REV_NUM_PATTERN = Pattern.compile("^Updated to revision ([0-9]+).*");

    private SubversionUtils() { }

    /**
     * Returns the revision number from the checkout output.
     *
     * @param output the checkout output
     *
     * @return the revision number or -1 if one could not be found
     */
    public static long getCheckoutRevision(final List<String> output) {
        return findRevision(output, CHECKOUT_REV_NUM_PATTERN);
    }

    /**
     * Returns the revision number from the commit output.
     *
     * @param output the commit output
     *
     * @return the revision number or -1 if one could not be found
     */
    public static long getCommitRevision(final List<String> output) {
        return findRevision(output, COMMIT_REV_NUM_PATTERN);
    }

    /**
     * Returns the revision number from the update output.
     *
     * @param output the checkout output
     *
     * @return the revision number or -1 if one could not be found
     */
    public static long getUpdateRevision(final List<String> output) {
        long rev = findRevision(output, UPDATE_REV_NUM_PATTERN);

        if (rev == -1) {
            rev = findRevision(output, AT_REV_NUM_PATTERN);
        }

        return rev;
    }

    /**
     * For the given pattern, find the revision number.
     *
     * @param output the output to parse
     * @param pattern the pattern to use
     *
     * @return the revision number or -1 if one could not be found
     */
    public static long findRevision(final List<String> output, final Pattern pattern) {
        long revision = -1;

        for (final String line : output) {
            final Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {
                revision = Long.parseLong(matcher.group(1));
                break;
            }
        }

        return revision;
    }

    /**
     * Indicates if path is absolute or a relative.
     */
    public static boolean isRelativePath(final String path) {
        return path.startsWith("^/");
    }

    /**
     * Combines {@code repoRoot} and {@code relativeProjectPath} and returns absolute project path.
     * {@code relativeProjectPath} can point to a branch, tag or any directory inside a project:
     *          ^/project/trunk
     *          ^/project/dir1/dir2
     *          ^/project/branches/1.0-SNAPSHOT
     *          ^/project/tags/2.0
     *          ^/project
     * The important thing that the first entry of the relative path is treated as a project name.
     * Otherwise the {@code repoRoot} will be returned as an absolute project path:
     *          ^/
     *          ^/trunk
     *
     * @param repoRoot
     *      the repository uri
     * @param relativeProjectPath
     *      the relative project path
     * @return absolute project uri
     */
    @Nullable
    public static String recognizeProjectUri(@Nullable final String repoRoot,
                                             @Nullable final String relativeProjectPath) {

        if (isNullOrEmpty(repoRoot) || isNullOrEmpty(relativeProjectPath)) {
            return null;
        }

        checkState(isRelativePath(relativeProjectPath), "Illegal relative project path " + relativeProjectPath);

        String[] entries = relativeProjectPath.split("/");
        if (entries.length == 1) {
            return repoRoot;
        }

        String candidateToProjectName = entries[1];
        if (candidateToProjectName.equals("trunk") || candidateToProjectName.equals("branches") || candidateToProjectName.equals("tags")) {
            return repoRoot;
        }

        return repoRoot + "/" + candidateToProjectName;
    }
}
