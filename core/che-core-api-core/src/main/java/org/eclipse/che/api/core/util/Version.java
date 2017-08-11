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
 * Version format:
 * {MajorVersion}.{MinorVersion}.{BugFixVersion}.{HotFixVersion}-M{MilestoneVersion}-beta-{BetaVersion}-RC-SNAPSHOT.
 * </p>
 * Mandatory parts are only major, minor and bug fix versions.
 *
 * @see #toString()
 * @see #VERSION
 *
 * @author Anatoliy Bazko
 */
public class Version implements Comparable<Version> {

    private static final String MILESTONE_VERSION_SUFFIX = "-M";
    private static final String BETA_VERSION_SUFFIX      = "-beta-";

    private static final Pattern VERSION =
            compile("^(?<major>0|[1-9]+[0-9]*)\\.(?<minor>0|[1-9]+[0-9]*)\\.(?<bugfix>0|[1-9]+[0-9]*)(?<hotfix>\\.0|\\.[1-9]+[0-9]*)?" +
                    "(?<milestone>" + MILESTONE_VERSION_SUFFIX + "[1-9]+[0-9]*)?" +
                    "(?<beta>" + BETA_VERSION_SUFFIX + "[1-9]+[0-9]*)?" +
                    "(?<rc>-RC)?" +
                    "(?<snapshot>-SNAPSHOT)?$");

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
    public static void validate(@NotNull String version) throws IllegalArgumentException {
        if (!VERSION.matcher(version).matches()) {
            throw new IllegalArgumentException(format("Illegal version '%s' format.", version));
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
            throw new IllegalArgumentException(format("Illegal version '%s' format.", version));
        }

        int hotFix = 0;
        int milestone = 0;
        int beta = 0;

        String hotFixGroup = matcher.group("hotfix");
        if (hotFixGroup != null) {
            hotFix = parseInt(hotFixGroup.substring(1));
        }

        String milestoneGroup = matcher.group("milestone");
        if (milestoneGroup != null) {
            milestone = parseInt(milestoneGroup.substring(MILESTONE_VERSION_SUFFIX.length()));
        }

        String betaGroup = matcher.group("beta");
        if (betaGroup != null) {
            beta = parseInt(betaGroup.substring(BETA_VERSION_SUFFIX.length()));
        }

        return new Version(parseInt(matcher.group("major")),
                           parseInt(matcher.group("minor")),
                           parseInt(matcher.group("bugfix")),
                           hotFix,
                           milestone,
                           beta,
                           matcher.group("rc") != null,
                           matcher.group("snapshot") != null);
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
        if (this == o) return true;
        if (!(o instanceof Version)) return false;
        Version version = (Version)o;
        return major == version.major &&
               minor == version.minor &&
               bugFix == version.bugFix &&
               hotFix == version.hotFix &&
               milestone == version.milestone &&
               beta == version.beta &&
               snapshot == version.snapshot &&
               rc == version.rc;
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
        if (major > o.major) {
            return 1;
        } else if (major < o.major) {
            return -1;
        }

        if (minor > o.minor) {
            return 1;
        } else if (minor < o.minor) {
            return -1;
        }

        if (bugFix > o.bugFix) {
            return 1;
        } else if (bugFix < o.bugFix) {
            return -1;
        }

        if (hotFix > o.hotFix) {
            return 1;
        } else if (hotFix < o.hotFix) {
            return -1;
        }

        // existence milestone version is considered lower in comparision with its absence
        if (milestone == 0 && o.milestone != 0) {
            return 1;
        } else if (milestone != 0 && o.milestone == 0) {
            return -1;
        } else if (milestone > o.milestone) {
            return 1;
        } else if (milestone < o.milestone) {
            return -1;
        }

        // existence beta version is considered lower in comparision with its absence
        if (beta == 0 && o.beta != 0) {
            return 1;
        } else if (beta != 0 && o.beta == 0) {
            return -1;
        } else if (beta > o.beta) {
            return 1;
        } else if (beta < o.beta) {
            return -1;
        }

        // existence RC mark is considered lower in comparision with its absence
        if (!rc && o.rc) {
            return 1;
        } else if (rc && !o.rc) {
            return -1;
        }

        // existence SNAPSHOT mark is considered lower in comparision with its absence
        if (!snapshot && o.snapshot) {
            return 1;
        } else if (snapshot && !o.snapshot) {
            return -1;
        }

        return 0;
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
