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
package org.eclipse.che.plugin.internal.repository;

import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.ZipUtils;
import org.eclipse.che.plugin.internal.api.PluginConfiguration;
import org.eclipse.che.plugin.internal.api.PluginRepository;
import org.eclipse.che.plugin.internal.api.PluginRepositoryException;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.lang.String.format;
import static org.eclipse.che.plugin.internal.repository.PluginRepositoryImpl.EXTENSIONS_DIR;
import static org.eclipse.che.plugin.internal.repository.PluginRepositoryImpl.MACHINES_DIR;
import static org.eclipse.che.plugin.internal.repository.PluginRepositoryImpl.TEMPLATES_DIR;
import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests of the plugin repository
 * @author Florent Benoit
 */
public class PluginRepositoryImplTest {


    private static final String EXTENSION1_FILENAME = "extensions1.jar";
    private static final String EXTENSION2_FILENAME = "extension2.jar";

    private static final String TEMPLATE_FILENAME = "mytemplate.json";
    private static final String MACHINE_FOLDERNAME = "my-machine";
    private static final String MACHINE_FILENAME = "Dockerfile";


    @Test
    public void testAvailablePlugins() throws Exception {

        try (PluginSetup pluginSetup = new PluginSetup()) {
            PluginRepository pluginRepository = pluginSetup.getRepository();

            List<Path> availablePlugins = pluginRepository.getAvailablePlugins();
            assertEquals(availablePlugins.size(), 0);

            // add a plugin
            Path tmpFile = Files.createTempFile("plugintext", ".jar");
            Path availablePlugin = pluginRepository.add(tmpFile);

            // check it's there
            availablePlugins = pluginRepository.getAvailablePlugins();
            assertEquals(availablePlugins.size(), 1);
            assertNotNull(availablePlugin);
        }


    }


    @Test
    public void testStagedPlugins() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            PluginRepository pluginRepository = pluginSetup.getRepository();

            // add a plugin
            Path tmpFile = Files.createTempFile("plugintext", ".jar");
            Path availablePlugin = pluginRepository.add(tmpFile);

            List<Path> stagedPlugins = pluginRepository.getStagedInstallPlugins();
            assertEquals(stagedPlugins.size(), 0);

            Path stagedPlugin = pluginRepository.stageInstall(availablePlugin);
            stagedPlugins = pluginRepository.getStagedInstallPlugins();
            assertEquals(stagedPlugins.size(), 1);
            assertNotNull(stagedPlugin);


        }
    }

    @Test
    public void testInstalledPlugins() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            PluginRepository pluginRepository = pluginSetup.getRepository();

            // add a plugin
            Path tmpFile = Files.createTempFile("plugintext", ".jar");
            Path availablePlugin = pluginRepository.add(tmpFile);
            assertNotNull(availablePlugin);

            Path stagedPlugin = pluginRepository.stageInstall(availablePlugin);
            assertNotNull(stagedPlugin);

            List<Path> installedPlugins = pluginRepository.getInstalledPlugins();
            assertEquals(installedPlugins.size(), 0);

            // require to install all plugins
            pluginRepository.preStaged();
            pluginRepository.stagedComplete();

            // it is installed
            installedPlugins = pluginRepository.getInstalledPlugins();
            assertEquals(installedPlugins.size(), 1);


        }
    }



    @Test
    public void testUninstalledPlugins() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            PluginRepository pluginRepository = pluginSetup.getRepository();

            // install a plugin
            Path tmpFile = Files.createTempFile("plugintext", ".jar");
            Path availablePlugin = pluginRepository.add(tmpFile);
            pluginRepository.stageInstall(availablePlugin);
            pluginRepository.preStaged();
            pluginRepository.stagedComplete();
            Path installedPlugin = pluginRepository.getInstalledPlugins().get(0);


            // check we've 1 installed plugin and zero available
            assertEquals(pluginRepository.getAvailablePlugins().size(), 0);
            assertEquals(pluginRepository.getInstalledPlugins().size(), 1);


            // now uninstall it
            pluginRepository.stageUninstall(installedPlugin);
            pluginRepository.stagedComplete();

            // check we've 0 installed plugin and 1 available
            assertEquals(pluginRepository.getAvailablePlugins().size(), 1);
            assertEquals(pluginRepository.getInstalledPlugins().size(), 0);


        }
    }




    @Test
    public void testRemove() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            PluginRepository pluginRepository = pluginSetup.getRepository();

            // add a plugin
            Path tmpFile = Files.createTempFile("plugintext", ".jar");

            Path availablePlugin = pluginRepository.add(tmpFile);
            assertNotNull(availablePlugin);

            List<Path> availablePlugins = pluginRepository.getAvailablePlugins();
            assertEquals(availablePlugins.size(), 1);


            pluginRepository.remove(availablePlugin);
            availablePlugins = pluginRepository.getAvailablePlugins();
            assertEquals(availablePlugins.size(), 0);

        }
    }


    protected Path createZipPlugin() throws IOException {

        // create directory for storing zip
        Path tmpPluginDir = Files.createTempDirectory("zipDir");

        // add extensions
        Path extensionsDir = Files.createDirectory(tmpPluginDir.resolve(EXTENSIONS_DIR));
        Files.createFile(extensionsDir.resolve(EXTENSION1_FILENAME));
        Files.createFile(extensionsDir.resolve(EXTENSION2_FILENAME));

        // add templates
        Path templatesDir = Files.createDirectory(tmpPluginDir.resolve(TEMPLATES_DIR));
        Files.createFile(templatesDir.resolve(TEMPLATE_FILENAME));

        // add machines
        Path machinesDir = Files.createDirectory(tmpPluginDir.resolve(MACHINES_DIR));
        Path machinesSubDir = Files.createDirectory(machinesDir.resolve(MACHINE_FOLDERNAME));
        Files.createFile(machinesSubDir.resolve(MACHINE_FILENAME));

        // create zip plugin
        Path pluginZipFile = Files.createTempFile("plugintext", ".zip");
        ZipUtils.zipFiles(pluginZipFile.toFile(), extensionsDir.toFile(), templatesDir.toFile(), machinesDir.toFile());
        IoUtil.deleteRecursive(tmpPluginDir.toFile());

        return pluginZipFile;
    }




    @Test
    public void testInstallUninstallPluginZip() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            PluginRepository pluginRepository = pluginSetup.getRepository();

            Path pluginZipFile = createZipPlugin();
            Path availablePlugin = pluginRepository.add(pluginZipFile);
            assertNotNull(availablePlugin);

            Path stagedPlugin = pluginRepository.stageInstall(availablePlugin);
            assertNotNull(stagedPlugin);

            List<Path> installedPlugins = pluginRepository.getInstalledPlugins();
            assertEquals(installedPlugins.size(), 0);

            // require to install all plugins
            pluginRepository.preStaged();
            pluginRepository.stagedComplete();

            // it is installed
            installedPlugins = pluginRepository.getInstalledPlugins();
            assertEquals(installedPlugins.size(), 1);

            // now uninstall it
            pluginRepository.stageUninstall(installedPlugins.get(0));
            assertEquals(pluginRepository.getStagedUninstallPlugins().size(), 1);

            // require to uninstall all plugins
            pluginRepository.preStaged();
            pluginRepository.stagedComplete();

            assertEquals(pluginRepository.getInstalledPlugins().size(), 0);
            assertEquals(pluginRepository.getAvailablePlugins().size(), 1);

            pluginRepository.remove(availablePlugin);
        }

    }



    @Test
    public void testInstallExtensionsFromZip() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            PluginConfiguration pluginConfiguration = pluginSetup.getPluginConfiguration();
            PluginRepositoryImpl pluginRepository = new PluginRepositoryImpl(pluginConfiguration);

            Path pluginZipFile = createZipPlugin();

            // install extensions from the zip file
            pluginRepository.installExtensionsFromZip(pluginZipFile);
            Files.delete(pluginZipFile);

            // Check that extension, machines and templates of plugin are part of the che home folder
            Path plugin1InExtFolder = pluginConfiguration.getExtRootFolder().resolve(EXTENSION1_FILENAME);
            Path plugin2InExtFolder = pluginConfiguration.getExtRootFolder().resolve(EXTENSION2_FILENAME);
            Path templateInTemplatesFolder = pluginConfiguration.getTemplatesRootFolder().resolve(TEMPLATE_FILENAME);
            Path machineFolder = pluginConfiguration.getMachinesRootFolder().resolve(MACHINE_FOLDERNAME);
            Path machine1Filename = machineFolder.resolve(MACHINE_FILENAME);
            assertTrue(Files.exists(plugin1InExtFolder), format("Extension %s should have been copied", EXTENSION1_FILENAME));
            assertTrue(Files.exists(plugin2InExtFolder), format("Extension %s should have been copied", EXTENSION2_FILENAME));
            assertTrue(Files.exists(templateInTemplatesFolder), format("Template %s should have been copied", TEMPLATE_FILENAME));
            assertTrue(Files.exists(machineFolder), format("Machine folder %s should have been copied", MACHINE_FOLDERNAME));
            assertTrue(Files.exists(machine1Filename), format("Machine filename %s should have been copied", machine1Filename));
        }

    }


    @Test
    public void testUninstallExtensionsFromZip() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            PluginConfiguration pluginConfiguration = pluginSetup.getPluginConfiguration();
            PluginRepositoryImpl pluginRepository = new PluginRepositoryImpl(pluginConfiguration);

            Path pluginZipFile = createZipPlugin();
            // first install extension
            pluginRepository.installExtensionsFromZip(pluginZipFile);

            // and then uninstall extensions from the zip file
            pluginRepository.uninstallExtensionsFromZip(pluginZipFile);

            // Check that extension, machines and templates of plugin are no longer part of the che home folder
            Path plugin1InExtFolder = pluginConfiguration.getExtRootFolder().resolve(EXTENSION1_FILENAME);
            Path plugin2InExtFolder = pluginConfiguration.getExtRootFolder().resolve(EXTENSION2_FILENAME);
            Path templateInTemplatesFolder = pluginConfiguration.getTemplatesRootFolder().resolve(TEMPLATE_FILENAME);
            Path machineFolder = pluginConfiguration.getMachinesRootFolder().resolve(MACHINE_FOLDERNAME);
            Path machine1Filename = machineFolder.resolve(MACHINE_FILENAME);
            assertTrue(Files.notExists(plugin1InExtFolder), format("Extension %s should have been removed", EXTENSION1_FILENAME));
            assertTrue(Files.notExists(plugin2InExtFolder), format("Extension %s should have been removed", EXTENSION2_FILENAME));
            assertTrue(Files.notExists(templateInTemplatesFolder), format("Template %s should have been removed", TEMPLATE_FILENAME));
            assertTrue(Files.notExists(machineFolder), format("Machine folder %s should have been removed", MACHINE_FOLDERNAME));
            assertTrue(Files.notExists(machine1Filename), format("Machine filename %s should have been removed", machine1Filename));

            Files.delete(pluginZipFile);
        }

    }


    @Test
    public void testUndoStageInstall() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            PluginRepository pluginRepository = pluginSetup.getRepository();

            Path pluginZipFile = createZipPlugin();

            // add
            Path availablePlugin = pluginRepository.add(pluginZipFile);
            assertNotNull(availablePlugin);
            assertEquals(pluginRepository.getAvailablePlugins().size(), 1);
            assertEquals(pluginRepository.getStagedInstallPlugins().size(), 0);

            // stage install
            Path stagedPlugin = pluginRepository.stageInstall(availablePlugin);
            assertNotNull(stagedPlugin);
            assertEquals(pluginRepository.getAvailablePlugins().size(), 0);
            assertEquals(pluginRepository.getStagedInstallPlugins().size(), 1);

            // undo
            pluginRepository.undoStageInstall(stagedPlugin);
            assertEquals(pluginRepository.getAvailablePlugins().size(), 1);
            assertEquals(pluginRepository.getStagedInstallPlugins().size(), 0);

            pluginRepository.remove(availablePlugin);

        }
    }



    @Test
    public void testUndoStageUninstallPluginZip() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            PluginRepository pluginRepository = pluginSetup.getRepository();

            Path pluginZipFile = createZipPlugin();
            Path availablePlugin = pluginRepository.add(pluginZipFile);
            pluginRepository.stageInstall(availablePlugin);
            pluginRepository.preStaged();
            pluginRepository.stagedComplete();

            // now stage for uninstall
            assertEquals(pluginRepository.getInstalledPlugins().size(), 1);
            assertEquals(pluginRepository.getStagedUninstallPlugins().size(), 0);
            Path stagedUninstall = pluginRepository.stageUninstall(pluginRepository.getInstalledPlugins().get(0));
            assertEquals(pluginRepository.getStagedUninstallPlugins().size(), 1);
            assertEquals(pluginRepository.getInstalledPlugins().size(), 0);

            // require to uninstall all plugins
            Path restoredInstall = pluginRepository.undoStageUninstall(stagedUninstall);
            assertEquals(pluginRepository.getStagedUninstallPlugins().size(), 0);
            assertEquals(pluginRepository.getInstalledPlugins().size(), 1);

            pluginRepository.remove(restoredInstall);
        }

    }


    /**
     * Here we test the case where something has failed during the staging operation (like mvn script fails)
     * We should ensure the system is restored as before preStaged() operation
     */
    @Test
    public void testStagedInstallingFailed() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            PluginConfiguration pluginConfiguration = pluginSetup.getPluginConfiguration();
            PluginRepositoryImpl pluginRepository = new PluginRepositoryImpl(pluginConfiguration);

            Path pluginZipFile = createZipPlugin();
            Path availablePlugin = pluginRepository.add(pluginZipFile);
            Path stagedFile = pluginRepository.stageInstall(availablePlugin);
            pluginRepository.preStaged();

            // check that plugin files are copied into ext folder
            Path plugin1InExtFolder = pluginConfiguration.getExtRootFolder().resolve(EXTENSION1_FILENAME);
            Path plugin2InExtFolder = pluginConfiguration.getExtRootFolder().resolve(EXTENSION2_FILENAME);
            assertTrue(Files.exists(plugin1InExtFolder), "extensions should have been copied");
            assertTrue(Files.exists(plugin2InExtFolder), "extensions should have been copied");

            // now staging has failed, call failed
            pluginRepository.stagedFailed();
            assertTrue(Files.notExists(plugin1InExtFolder), "extensions should have been removed as staging has failed");
            assertTrue(Files.notExists(plugin2InExtFolder), "extensions should have been removed as staging has failed");

            Files.delete(stagedFile);

        }
    }

    /**
     * Here we test the case where something has failed during the staging operation (like mvn script fails)
     * We should ensure the system is restored as before preStaged() operation
     */
    @Test
    public void testStagedUninstallingFailed() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            PluginConfiguration pluginConfiguration = pluginSetup.getPluginConfiguration();
            PluginRepositoryImpl pluginRepository = new PluginRepositoryImpl(pluginConfiguration);

            Path pluginZipFile = createZipPlugin();
            Path availablePlugin = pluginRepository.add(pluginZipFile);
            pluginRepository.stageInstall(availablePlugin);
            pluginRepository.preStaged();
            pluginRepository.stagedComplete();
            // here plugins has been installed, now try to remove them
            assertEquals(pluginRepository.getInstalledPlugins().size(), 1);


            // ask to uninstall plugin
            Path stageUninstall = pluginRepository.stageUninstall(pluginRepository.getInstalledPlugins().get(0));
            pluginRepository.preStaged();

            // check that plugin files are no longer in ext folder
            Path plugin1InExtFolder = pluginConfiguration.getExtRootFolder().resolve(EXTENSION1_FILENAME);
            Path plugin2InExtFolder = pluginConfiguration.getExtRootFolder().resolve(EXTENSION2_FILENAME);
            assertTrue(Files.notExists(plugin1InExtFolder), "extensions should have been removed");
            assertTrue(Files.notExists(plugin2InExtFolder), "extensions should have been removed");

            // now staging has failed, call failed
            pluginRepository.stagedFailed();
            assertTrue(Files.exists(plugin1InExtFolder), "extensions should have been restored as staging has failed");
            assertTrue(Files.exists(plugin2InExtFolder), "extensions should have been restored as staging has failed");

            Files.delete(stageUninstall);
        }
    }


    @Test(expectedExceptions = PluginRepositoryException.class)
    public void testInvalidAdd() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
                pluginSetup.getRepository().add(pluginSetup.getPluginConfiguration().getExtRootFolder().resolve("unknown-file"));
        }
    }

    @Test(expectedExceptions = PluginRepositoryException.class)
    public void testInvalidZipFile() throws Exception {
        try (PluginSetup pluginSetup = new PluginSetup()) {
            // create corrupted zip
            Path invalidFile = pluginSetup.getPluginConfiguration().getExtRootFolder().resolve("invalid.zip");
            FileOutputStream fos = new FileOutputStream(invalidFile.toFile());
            ZipOutputStream zos = new ZipOutputStream(fos);
            ZipEntry ze = new ZipEntry("invalid.txt");
            zos.putNextEntry(ze);
            byte[] data = "content".getBytes();
            fos.write(data, 0, data.length);
            zos.closeEntry();
            zos.finish();
            zos.close();


            pluginSetup.getRepository().installExtensionsFromZip(invalidFile);
        }

    }



    class PluginSetup implements AutoCloseable {

        private final PluginConfiguration pluginConfiguration;
        private final Path testFolder;


        public PluginSetup() throws IOException {
            this.pluginConfiguration = Mockito.mock(PluginConfiguration.class);
            this.testFolder = Files.createTempDirectory("che-test-folder");

            Path pluginFolder = Files.createDirectory(testFolder.resolve("che-plugins-root"));
            Path extFolder = Files.createDirectory(testFolder.resolve("che-ext-root"));
            Path templatesFolder = Files.createDirectory(testFolder.resolve("che-templates-root"));
            Path machinesFolder = Files.createDirectory(testFolder.resolve("che-machines-root"));
            doReturn(pluginFolder).when(pluginConfiguration).getPluginRootFolder();
            doReturn(extFolder).when(pluginConfiguration).getExtRootFolder();
            doReturn(templatesFolder).when(pluginConfiguration).getTemplatesRootFolder();
            doReturn(machinesFolder).when(pluginConfiguration).getMachinesRootFolder();

        }

        /**
         * Cleanup configuration
         */
        @Override
        public void close() throws Exception {
            IoUtil.deleteRecursive(testFolder.toFile());
        }

        public PluginConfiguration getPluginConfiguration() {
            return pluginConfiguration;
        }

        public PluginRepositoryImpl getRepository() throws PluginRepositoryException {
            return new PluginRepositoryImpl(pluginConfiguration);
        }
    }



}
