/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.internal.resolver;

import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.plugin.internal.api.PluginConfiguration;
import org.eclipse.che.plugin.internal.api.PluginResolverNotFoundException;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * @author Florent Benoit
 */
public class MavenResolverTest {


    /**
     * Check protocol handled by maven resolver
     */
    @Test
    public void testProtocol() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            MavenResolver mavenResolver = getResolver(pluginSetup.getPluginConfiguration());
            assertEquals(mavenResolver.getProtocol(), "mvn:");
        }
    }

    /**
     * Check protocol handled by maven resolver
     */
    @Test
    public void testDownload() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            MavenResolver mavenResolver = getResolver(pluginSetup.getPluginConfiguration());
            Path downloadedFile = mavenResolver.download("mvn:org.apache.maven:maven:pom:3.3.3");
            assertNotNull(downloadedFile);
            assertEquals(downloadedFile.getFileName().toString(), "maven-3.3.3.pom");

        }
    }



    /**
     * Check protocol handled by maven resolver
     */
    @Test(expectedExceptions = PluginResolverNotFoundException.class)
    public void testInvalidDownload() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            MavenResolver mavenResolver = getResolver(pluginSetup.getPluginConfiguration());
            mavenResolver.download("mvn:org.apache.maven:maven:pom:3.3.error");
            fail("Unable to download that file");

        }
    }

    class PluginSetup implements AutoCloseable {

        private final PluginConfiguration pluginConfiguration;
        private final Path                 m2Folder;


        public PluginSetup() throws IOException {
            this.pluginConfiguration = Mockito.mock(PluginConfiguration.class);
            this.m2Folder = Files.createTempDirectory("tmpMavenFolder");
            doReturn(m2Folder).when(pluginConfiguration).getLocalMavenRepository();
        }

        /**
         * Cleanup configuration
         */
        @Override
        public void close() throws Exception {
            IoUtil.deleteRecursive(m2Folder.toFile());
        }

        public PluginConfiguration getPluginConfiguration() {
            return pluginConfiguration;
        }
    }

    MavenResolver getResolver(PluginConfiguration pluginConfiguration) {
        return new MavenResolver(pluginConfiguration);
    }


}
