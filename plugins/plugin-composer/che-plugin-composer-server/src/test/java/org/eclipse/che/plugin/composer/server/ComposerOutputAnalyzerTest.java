/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.composer.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Testing {@link ComposerOutputAnalyzer} functionality.
 *
 * @author Kaloyan Raev
 */
public class ComposerOutputAnalyzerTest {

    @Test
    public void testNoError() throws IOException {
        ComposerOutputAnalyzer.checkForErrors(0, Collections.emptyList());
    }

    @Test
    public void testNonExistingPackage() {
        try {
            ComposerOutputAnalyzer.checkForErrors(1, Arrays.asList(
                    "",
                    "                                                                      ",
                    "  [InvalidArgumentException]                                          ",
                    "  Could not find package non-existing/package with stability stable.  ",
                    "                                                                      ",
                    "",
                    "create-project [-s|--stability STABILITY] [--prefer-source] [--prefer-dist] [--repository REPOSITORY] [--repository-url REPOSITORY-URL] [--dev] [--no-dev] [--no-custom-installers] [--no-scripts] [--no-progress] [--no-secure-http] [--keep-vcs] [--no-install] [--ignore-platform-reqs] [--] [<package>] [<directory>] [<version>]",
                    ""
            ));
            fail("IOException expected.");
        } catch (IOException e) {
            assertEquals("Could not find package non-existing/package with stability stable.", e.getMessage());
        }
    }

    @Test
    public void testWrongOption() {
        try {
            ComposerOutputAnalyzer.checkForErrors(1, Arrays.asList(
                    "",
                    "                                               ",
                    "  [InvalidArgumentException]                   ",
                    "  The \"--wrong-option\" option does not exist.  ",
                    "                                               ",
                    "",
                    "create-project [-s|--stability STABILITY] [--prefer-source] [--prefer-dist] [--repository REPOSITORY] [--repository-url REPOSITORY-URL] [--dev] [--no-dev] [--no-custom-installers] [--no-scripts] [--no-progress] [--no-secure-http] [--keep-vcs] [--no-install] [--ignore-platform-reqs] [--] [<package>] [<directory>] [<version>]",
                    ""
            ));
            fail("IOException expected.");
        } catch (IOException e) {
            assertEquals("The \"--wrong-option\" option does not exist.", e.getMessage());
        }
    }

    @Test
    public void testDirectoryNotEmpty() {
        try {
            ComposerOutputAnalyzer.checkForErrors(1, Arrays.asList(
                    "Installing laravel/laravel (v5.3.16)",
                    "",
                    "                                                      ",
                    "  [InvalidArgumentException]                          ",
                    "  Project directory /projects/laravel/ is not empty.  ",
                    "                                                      ",
                    "",
                    "create-project [-s|--stability STABILITY] [--prefer-source] [--prefer-dist] [--repository REPOSITORY] [--repository-url REPOSITORY-URL] [--dev] [--no-dev] [--no-custom-installers] [--no-scripts] [--no-progress] [--no-secure-http] [--keep-vcs] [--no-install] [--ignore-platform-reqs] [--] [<package>] [<directory>] [<version>]",
                    ""
            ));
            fail("IOException expected.");
        } catch (IOException e) {
            assertEquals("Project directory /projects/laravel/ is not empty.", e.getMessage());
        }
    }

    @Test
    public void testDependencyProblem() {
        try {
            ComposerOutputAnalyzer.checkForErrors(2, Arrays.asList(
                    "Installing kaloyan-raev/astrosplash (1.0)",
                    "  - Installing kaloyan-raev/astrosplash (1.0)",
                    "    Loading from cache",
                    "",
                    "Created project in /projects/astrosplash",
                    "Loading composer repositories with package information",
                    "Installing dependencies (including require-dev) from lock file",
                    "Warning: The lock file is not up to date with the latest changes in composer.json. You may be getting outdated dependencies. Run update to update them.",
                    "Your requirements could not be resolved to an installable set of packages.",
                    "",
                    "  Problem 1",
                    "    - Installation request for ocramius/package-versions 1.1.1 -> satisfiable by ocramius/package-versions[1.1.1].",
                    "    - ocramius/package-versions 1.1.1 requires php ~7.0 -> your PHP version (5.6.24) does not satisfy that requirement.",
                    "  Problem 2",
                    "    - Installation request for ocramius/proxy-manager 2.0.1 -> satisfiable by ocramius/proxy-manager[2.0.1].",
                    "    - ocramius/proxy-manager 2.0.1 requires php ~7.0 -> your PHP version (5.6.24) does not satisfy that requirement.",
                    ""
            ));
            fail("IOException expected.");
        } catch (IOException e) {
            assertEquals("Your requirements could not be resolved to an installable set of packages.\n" +
                    "\n" +
                    "  Problem 1\n" +
                    "    - Installation request for ocramius/package-versions 1.1.1 -> satisfiable by ocramius/package-versions[1.1.1].\n" +
                    "    - ocramius/package-versions 1.1.1 requires php ~7.0 -> your PHP version (5.6.24) does not satisfy that requirement.\n" +
                    "  Problem 2\n" +
                    "    - Installation request for ocramius/proxy-manager 2.0.1 -> satisfiable by ocramius/proxy-manager[2.0.1].\n" +
                    "    - ocramius/proxy-manager 2.0.1 requires php ~7.0 -> your PHP version (5.6.24) does not satisfy that requirement.\n" +
                    "", e.getMessage());
        }
    }

    @Test
    public void testUnexpectedErrors() {
        for (int i = 1; i < 256; i++) {
            unexpectedError(i);
        }
    }

    private void unexpectedError(int exitCode) {
        try {
            ComposerOutputAnalyzer.checkForErrors(exitCode, Collections.emptyList());
            fail(String.format("IOException expected for exit code %d.", exitCode));
        } catch (IOException e) {
            assertEquals(String.format("Composer exited with code %d.", exitCode), e.getMessage());
        }
    }

}
