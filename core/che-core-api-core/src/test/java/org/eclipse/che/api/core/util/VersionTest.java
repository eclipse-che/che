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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 */
public class VersionTest {
    @Test(dataProvider = "testValidVersion")
    public void testValidVersion(String version) throws Exception {
        Version.validate(version);
    }

    @DataProvider(name = "testValidVersion")
    public static Object[][] testValidVersion() {
        return new Object[][] {{"0.0.1"},
                               {"1.0.1"},
                               {"10.3.0"},
                               {"10.3.0.0"},
                               {"10.3.0.1"},
                               {"10.3.0.1-RC"},
                               {"0.9.0"},
                               {"1.0.0"},
                               {"1.0.10"},
                               {"1.0.10-RC"},
                               {"1.0.1-SNAPSHOT"},
                               {"1.0.1-RC-SNAPSHOT"},
                               {"1.0.1.0-SNAPSHOT"},
                               {"1.0.1-M1"},
                               {"1.0.1.1-M1"},
                               {"1.0.1-M1-SNAPSHOT"},
                               {"1.0.1.2-M1-SNAPSHOT"},
                               {"1.0.1.2-beta-1-SNAPSHOT"},
                               {"1.0.1.2-M1-beta-1-SNAPSHOT"},
                               {"1.0.1.2-M1-RC-SNAPSHOT"}};
    }

    @Test(dataProvider = "testInvalidVersion", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidVersion(String version) throws Exception {
        Version.validate(version);
    }

    @DataProvider(name = "testInvalidVersion")
    public static Object[][] testInvalidVersion() {
        return new Object[][] {{"1"},
                               {"00.1.1"},
                               {"1.1"},
                               {"1.1."},
                               {"1.1.1."},
                               {"1.01.1"},
                               {"01.1.1"},
                               {"1.1.01"},
                               {"1.0.1-"},
                               {"1.0.1-M"},
                               {"1.0.1-M0"},
                               {"1.0.1-M-SNAPSHOT"},
                               {"1.0.1-M0-SNAPSHOT"},
                               {"1.0.1--SNAPSHOT"},
                               {"1.0.1-beta-0"},
                               {"1.0.1-SNAPSHOT-RC"}};
    }

    @Test(dataProvider = "testParseValidVersion")
    public void testParseValidVersion(String str,
                                      int major,
                                      int minor,
                                      int bugFix,
                                      int hotFix,
                                      int milestone,
                                      int beta,
                                      boolean rc,
                                      boolean snapshot) throws Exception {
        assertEquals(Version.parse(str), new Version(major, minor, bugFix, hotFix, milestone, beta, rc, snapshot));
    }


    @DataProvider(name = "testParseValidVersion")
    public Object[][] testParseValidVersion() {
        return new Object[][] {
                {"1.0.1-RC", 1, 0, 1, 0, 0, 0, true, false},
                {"1.0.1.0", 1, 0, 1, 0, 0, 0, false, false},
                {"10.150.200.1", 10, 150, 200, 1, 0, 0, false, false},
                {"10.150.200.24-SNAPSHOT", 10, 150, 200, 24, 0, 0, false, true},
                {"10.150.200-M20", 10, 150, 200, 0, 20, 0, false, false},
                {"10.150.200-M20-RC", 10, 150, 200, 0, 20, 0, true, false},
                {"10.150.200-M20-SNAPSHOT", 10, 150, 200, 0, 20, 0, false, true},
                {"10.150.200-beta-1-SNAPSHOT", 10, 150, 200, 0, 0, 1, false, true},
                {"10.150.200-M20-RC-SNAPSHOT", 10, 150, 200, 0, 20, 0, true, true}};
    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseInvalidVersion() throws Exception {
        Version.parse("01.1.1");
    }

    @Test(dataProvider = "testToString")
    public void testToString(String str) throws Exception {
        assertEquals(Version.parse(str).toString(), str);
    }

    @DataProvider(name = "testToString")
    public Object[][] testToString() {
        return new Object[][] {
                {"10.150.200"},
                {"10.150.200.1"},
                {"10.150.200-M20-SNAPSHOT"},
                {"10.150.200.20-M20-SNAPSHOT"},
                {"10.150.200-M20"},
                {"10.150.200-SNAPSHOT"},
                {"10.150.200-beta-1"},
                {"10.150.200-RC-SNAPSHOT"}};
    }


    @Test(dataProvider = "testCompareTo")
    public void testCompareTo(String version1, String version2, int expected) throws Exception {
        assertEquals(Version.parse(version1).compareTo(Version.parse(version2)), expected, version1 + " vs " + version2);
    }

    @DataProvider(name = "testCompareTo")
    public Object[][] testCompareTo() {
        return new Object[][] {
                {"1.0.1", "1.0.1", 0},
                {"1.0.1", "1.0.1.0", 0},
                {"1.0.2-M20", "1.0.2-M20", 0},
                {"1.0.2-M20-SNAPSHOT", "1.0.2-M20-SNAPSHOT", 0},
                {"1.0.2.4-M20-SNAPSHOT", "1.0.2.4-M20-SNAPSHOT", 0},
                {"1.0.2.4-M20-RC-SNAPSHOT", "1.0.2.4-M20-RC-SNAPSHOT", 0},
                {"1.0.2-SNAPSHOT", "1.0.2-SNAPSHOT", 0},

                {"2.0.1", "1.0.1", 1},
                {"1.1.1", "1.0.1", 1},
                {"1.0.1.1", "1.0.1", 1},
                {"1.0.1.1", "1.0.1.0", 1},
                {"1.0.2", "1.0.1", 1},
                {"1.0.2", "1.0.1-RC", 1},
                {"1.0.2", "1.0.1-M20", 1},
                {"1.0.2", "1.0.2-SNAPSHOT", 1},
                {"1.0.2-M20", "1.0.2-M19", 1},
                {"1.0.2-M20", "1.0.2-M20-SNAPSHOT", 1},
                {"1.0.2-M20", "1.0.2-M20-RC", 1},
                {"1.0.2-beta-2", "1.0.2-beta-1", 1},

                {"1.0.1", "2.0.1", -1},
                {"1.0.1", "1.1.1", -1},
                {"1.1.1.0", "1.1.1.1", -1},
                {"1.0.1", "1.0.2", -1},
                {"1.0.1-SNAPSHOT", "1.0.1", -1},
                {"1.0.2-M20", "1.0.2", -1},
                {"1.0.2-beta-2", "1.0.2", -1}};
    }
}
