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
package org.eclipse.che.plugin.svn.server.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

}
