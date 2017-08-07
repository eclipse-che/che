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
package org.eclipse.che.api.core.util;

import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;

/**
 * @author Anatoliy Bazko
 */
public class Version implements Comparable<Version> {

    private static final String MILESTONE_VERSION_SUFFIX = "-M";
    private static final String BETA_VERSION_SUFFIX      = "-beta-";

    private static final Pattern VERSION =
            compile("^(0|[1-9]+[0-9]*)\\.(0|[1-9]+[0-9]*)\\.(0|[1-9]+[0-9]*)(\\.0|\\.[1-9]+[0-9]*|)" +
                    "(" + MILESTONE_VERSION_SUFFIX + "[1-9]+[0-9]*|)" +
                    "(" + BETA_VERSION_SUFFIX + "[1-9]+[0-9]*|)" +
                    "(-RC|)" +
                    "(-SNAPSHOT|)$");

    private final int     major;
    private final int     minor;
    private final int     bugFix;
    private final int     hotFix;
    private final int     milestone;
    private final int     beta;
    private final boolean snapshot;
    private final boolean rc;

    public Version(int major,
                   int minor,
                   int bugFix,
                   int hotFix,
                   int milestone,
                   int beta,
                   boolean rc,
                   boolean snapshot) {
        this.major = major;
        this.minor = minor;
        this.bugFix = bugFix;
        this.hotFix = hotFix;
        this.milestone = milestone;
        this.beta = beta;
        this.snapshot = snapshot;
        this.rc = rc;
    }

    /**
     * Checks if version format is valid.
     *
     * @throws IllegalArgumentException
     *      if {@code version} format is wrong
     */
    public static void validate(@NotNull String version) {
        if (!VERSION.matcher(version).matches()) {
            throw new IllegalArgumentException(format("Illegal version '%s'", version));
        }
    }

    /**
     * Parse version in string representation.
     *
     * @throws IllegalArgumentException
     *      if {@code version} format is wrong
     */
    public static Version parse(@NotNull String version) throws IllegalArgumentException {
        Matcher matcher = VERSION.matcher(version);
        if (!matcher.find()) {
            throw new IllegalArgumentException(format("Illegal version '%s'", version));
        }

        int hotFix = 0;
        int milestone = 0;
        int beta = 0;

        String hotFixGroup = matcher.group(4);
        if (!hotFixGroup.isEmpty()) {
            hotFix = parseInt(hotFixGroup.substring(1));
        }

        String milestoneGroup = matcher.group(5);
        if (!milestoneGroup.isEmpty()) {
            milestone = parseInt(milestoneGroup.substring(MILESTONE_VERSION_SUFFIX.length()));
        }

        String betaGroup = matcher.group(6);
        if (!betaGroup.isEmpty()) {
            beta = parseInt(betaGroup.substring(BETA_VERSION_SUFFIX.length()));
        }

        return new Version(parseInt(matcher.group(1)),
                           parseInt(matcher.group(2)),
                           parseInt(matcher.group(3)),
                           hotFix,
                           milestone,
                           beta,
                           !matcher.group(7).isEmpty(),
                           !matcher.group(8).isEmpty());
    }

    /**
     * Checks if version suites for pattern.
     * For example 3.1.0 version is suited for 3.1.* or 3.*.0
     */
    public boolean isSuitedFor(String versionRegex) {
        Pattern pattern = compile(versionRegex);
        return pattern.matcher(toString()).matches();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Version)) {
            return false;
        }

        Version version = (Version)o;

        if (bugFix != version.bugFix) {
            return false;
        }
        if (hotFix != version.hotFix) {
            return false;
        }
        if (major != version.major) {
            return false;
        }
        if (milestone != version.milestone) {
            return false;
        }
        if (beta != version.beta) {
            return false;
        }
        if (minor != version.minor) {
            return false;
        }
        if (rc != version.rc) {
            return false;
        }
        if (snapshot != version.snapshot) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + bugFix;
        result = 31 * result + hotFix;
        result = 31 * result + milestone;
        result = 31 * result + beta;
        result = 31 * result + (snapshot ? 1 : 0);
        result = 31 * result + (rc ? 1 : 0);
        return result;
    }

    @Override
    public int compareTo(Version o) {
        if (major > o.major
            || (major == o.major && minor > o.minor)
            || (major == o.major && minor == o.minor && bugFix > o.bugFix)
            || (major == o.major && minor == o.minor && bugFix == o.bugFix && hotFix > o.hotFix)
            || (major == o.major && minor == o.minor && bugFix == o.bugFix && hotFix == o.hotFix
                && (milestone == 0 && o.milestone != 0 || milestone != 0 && o.milestone != 0 && milestone > o.milestone))
            || (major == o.major && minor == o.minor && bugFix == o.bugFix && hotFix == o.hotFix && milestone == o.milestone
                && (beta == 0 && o.beta != 0 || beta != 0 && o.beta != 0 && beta > o.beta))
            || (major == o.major && minor == o.minor && bugFix == o.bugFix && hotFix == o.hotFix && milestone == o.milestone
                && beta == o.beta && !rc && o.rc)
            || (major == o.major && minor == o.minor && bugFix == o.bugFix && hotFix == o.hotFix && milestone == o.milestone
                && beta == o.beta && rc == o.rc && !snapshot && o.snapshot)) {
            return 1;
        } else if (major == o.major && minor == o.minor && bugFix == o.bugFix && hotFix == o.hotFix
                   && milestone == o.milestone && beta == o.beta && rc == o.rc && snapshot == o.snapshot) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return major
               + "." + minor
               + "." + bugFix
               + (hotFix > 0 ? "." + hotFix : "")
               + (milestone > 0 ? MILESTONE_VERSION_SUFFIX + milestone : "")
               + (beta > 0 ? BETA_VERSION_SUFFIX + beta : "")
               + (rc ? "-RC" : "")
               + (snapshot ? "-SNAPSHOT" : "");

    }

    public static class ReverseOrderComparator implements Comparator<Version> {
        @Override
        public int compare(Version v1, Version v2) {
            return v2.compareTo(v1);
        }
    }
}
