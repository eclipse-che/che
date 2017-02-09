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
package org.eclipse.che.plugin.svn.server;

import com.google.common.base.Joiner;

import org.eclipse.che.commons.lang.NameGenerator;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link org.eclipse.che.plugin.svn.server.SubversionConfigurationChecker}.
 *
 * @author Vladyslav Zhukovskyi
 */
public class SubversionConfigurationCheckerTest {

    private SubversionConfigurationChecker configurationChecker;

    @Before
    public void setup() {
        configurationChecker = new SubversionConfigurationChecker();
    }

    @Test
    public void testSvnConfigFileShouldExist() throws Exception {
        configurationChecker.ensureExistingSvnConfigFile();

        Path loadedConfigFile = configurationChecker.getLoadedConfigFile();

        assertTrue(loadedConfigFile.toFile().exists());
        assertTrue(loadedConfigFile.toFile().isFile());
    }

    @Test
    public void testSvnCustomConfigFileShouldExist() throws Exception {
        configurationChecker = new SubversionConfigurationChecker(
                Paths.get(System.getProperty("java.io.tmpdir"), NameGenerator.generate("svn-config-", 4)));
        configurationChecker.ensureExistingSvnConfigFile();

        Path loadedConfigFile = configurationChecker.getLoadedConfigFile();

        assertTrue(loadedConfigFile.toFile().exists());
        assertTrue(loadedConfigFile.toFile().isFile());
    }

    @Test
    public void testConstructProperConfigFile() throws Exception {
        configurationChecker = new SubversionConfigurationChecker(
                Paths.get(System.getProperty("java.io.tmpdir"), NameGenerator.generate("svn-config-", 4)));
        configurationChecker.ensureExistingSvnConfigFile();

        Path loadedConfigFile = configurationChecker.getLoadedConfigFile();

        assertTrue(loadedConfigFile.toFile().exists());
        assertTrue(loadedConfigFile.toFile().isFile());

        configurationChecker.checkAndUpdateConfigFile();

        List<String> configContent = Files.readAllLines(loadedConfigFile, Charset.forName("UTF-8"));

        String expectedContent = "[miscellany]\n" +
                                 "global-ignores = .che .vfs";

        assertEquals(expectedContent, Joiner.on('\n').join(configContent));
    }
}
