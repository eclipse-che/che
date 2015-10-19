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
package org.eclipse.che.plugin.internal.installer;

import com.google.common.util.concurrent.FutureCallback;

import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.plugin.internal.api.IPluginInstall;
import org.eclipse.che.plugin.internal.api.IPluginInstallStatus;
import org.eclipse.che.plugin.internal.api.PluginConfiguration;
import org.eclipse.che.plugin.internal.api.PluginInstallerNotFoundException;
import org.eclipse.che.plugin.internal.api.PluginRepositoryException;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static org.eclipse.che.plugin.internal.api.IPluginInstallStatus.FAILED;
import static org.eclipse.che.plugin.internal.api.IPluginInstallStatus.SUCCESS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Florent Benoit
 */
public class PluginInstallerImplTest {



    @Test
    public void testRequireInstallSuccess() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            PluginInstallerImpl pluginInstaller = pluginSetup.getInstallerValidScript();

            FutureCallback callback = mock(FutureCallback.class);

            IPluginInstall install = pluginInstaller.requireNewInstall(callback);
            Integer exitValue = install.getFuture().get();
            Throwable error = install.getError();
            Assert.assertEquals(exitValue.intValue(), 0, "script should be successfully exit");
            Assert.assertNull(error, "no error should be reported");
            Assert.assertEquals(install.getStatus(), SUCCESS);
            Assert.assertEquals(install.getLog(), "");
            verify(callback).onSuccess(any());
        }
    }



    @Test
    public void testRequireInstallFailureInScript() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            PluginInstallerImpl pluginInstaller = pluginSetup.getInstallerInvalidCommandInScript();

            FutureCallback callback = mock(FutureCallback.class);

            IPluginInstall install = pluginInstaller.requireNewInstall(callback);
            Integer exitValue = install.getFuture().get();
            Throwable error = install.getError();
            Assert.assertNotEquals(exitValue.intValue(), 0, "script should not be successfully exit");
            Assert.assertNull(error, "no error should be reported");
            Assert.assertEquals(install.getStatus(), FAILED);
            verify(callback).onFailure(any());

        }
    }


    @Test
    public void testRequireInstallFailureScriptNotFound() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            PluginInstallerImpl pluginInstaller = pluginSetup.getInstallerInvalidScript();
            FutureCallback callback = mock(FutureCallback.class);

            IPluginInstall install = pluginInstaller.requireNewInstall(callback);
            Throwable error = install.getError();

            Assert.assertNotNull(error, "no error should be reported");
            Assert.assertEquals(install.getStatus(), FAILED);
            verify(callback).onFailure(any());

        }
    }




    @Test
    public void testGetInstall() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            PluginInstallerImpl pluginInstaller = pluginSetup.getInstallerValidScript();

            FutureCallback callback = mock(FutureCallback.class);
            IPluginInstall install = pluginInstaller.requireNewInstall(callback);


            IPluginInstall retrievedInstall = pluginInstaller.getInstall(install.getId());
            Assert.assertNotNull(retrievedInstall);

            // try with invalid ID
            try {
                pluginInstaller.getInstall(123456L);
                Assert.fail("Invalid id should throw Exception");
            } catch (PluginInstallerNotFoundException e) {
                Assert.assertTrue(e.getMessage().contains("Unknown install"));
            }
        }
    }




    @Test
    public void testListInstall() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            PluginInstallerImpl pluginInstaller = pluginSetup.getInstallerValidScript();

            // no installs
            List<IPluginInstall> currentList = pluginInstaller.listInstall();
            Assert.assertEquals(currentList.size(), 0);

            // install one
            FutureCallback callback = mock(FutureCallback.class);
            IPluginInstall install = pluginInstaller.requireNewInstall(callback);

            // there should have one installs
            currentList = pluginInstaller.listInstall();
            Assert.assertEquals(currentList.size(), 1);
            IPluginInstall listElement = currentList.get(0);
            Assert.assertEquals(listElement.getId(), install.getId());
            Assert.assertEquals(listElement.getStatus(), IPluginInstallStatus.SUCCESS);


        }
    }



    class PluginSetup implements AutoCloseable {

        private final PluginConfiguration pluginConfiguration;
        private final Path cheFolder;
        private PluginInstallerImpl pluginInstaller;

        public PluginSetup() throws IOException {
            this.pluginConfiguration = Mockito.mock(PluginConfiguration.class);
            this.cheFolder = Files.createTempDirectory("che-home");

            doReturn(cheFolder).when(pluginConfiguration).getCheHome();

        }

        /**
         * Cleanup configuration
         */
        @Override
        public void close() throws Exception {
            IoUtil.deleteRecursive(cheFolder.toFile());
            if (this.pluginInstaller != null) {
                this.pluginInstaller.stop();
            }
        }

        public PluginConfiguration getPluginConfiguration() {
            return pluginConfiguration;
        }

        public PluginInstallerImpl getInstallerInvalidScript() throws PluginRepositoryException {
            Path script;
            if (SystemInfo.isWindows()) {
                script = cheFolder.resolve("unknown-script.bat");
            } else {
                script = cheFolder.resolve("unknown-script.sh");
            }
            doReturn(script).when(pluginConfiguration).getInstallScript();
            this.pluginInstaller = new PluginInstallerImpl(pluginConfiguration);
            return this.pluginInstaller;
        }

        public PluginInstallerImpl getInstallerValidScript() throws PluginRepositoryException, IOException {
            Path script;
            if (SystemInfo.isWindows()) {
                script = Files.createFile(cheFolder.resolve("testscript.bat"));
                FileWriter writer = new FileWriter(script.toFile());
                writer.write("@echo off");
                writer.close();
            } else {
                Set<PosixFilePermission> permissions = new HashSet<>();
                permissions.add(OWNER_EXECUTE);
                permissions.add(OWNER_READ);
                permissions.add(OWNER_WRITE);
                FileAttribute<Set<PosixFilePermission>> requested = PosixFilePermissions.asFileAttribute(permissions);
                script = Files.createFile(cheFolder.resolve("testscript.sh"), requested);

            }
            doReturn(script).when(pluginConfiguration).getInstallScript();
            this.pluginInstaller = new PluginInstallerImpl(pluginConfiguration);
            return this.pluginInstaller;
        }

        public PluginInstallerImpl getInstallerInvalidCommandInScript() throws PluginRepositoryException, IOException {
            PluginInstallerImpl installer = getInstallerValidScript();
            PluginConfiguration pluginConfiguration = installer.getPluginConfiguration();

            if (SystemInfo.isWindows()) {
                FileWriter writer = new FileWriter(pluginConfiguration.getInstallScript().toFile());
                writer.write("echooooinvalidcommmand.bat");
                writer.close();
            } else {
                FileWriter writer = new FileWriter(pluginConfiguration.getInstallScript().toFile());
                writer.write("/bin/invalid-command");
                writer.close();
            }

            this.pluginInstaller = new PluginInstallerImpl(pluginConfiguration);
            return this.pluginInstaller;
        }


    }


}