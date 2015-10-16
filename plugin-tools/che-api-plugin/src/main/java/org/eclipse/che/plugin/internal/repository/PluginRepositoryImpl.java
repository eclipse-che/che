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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * Manage plugin paths and copy/move/unzip, etc of plugins.
 * @author Florent Benoit
 */
@Singleton
public class PluginRepositoryImpl implements PluginRepository {

    /**
     * Defines name of the entries inside the plugin zip file.
     * Extensions contains all jars file (extensions)
     */
    protected static final String EXTENSIONS_DIR = "extensions";

    /**
     * Defines name of the entries inside the plugin zip file.
     * Machines contains all Dockerfile or machines files
     */
    protected static final String MACHINES_DIR = "machines";

    /**
     * Defines name of the entries inside the plugin zip file.
     * Templates contains all the project type templates
     */
    protected static final String TEMPLATES_DIR = "templates";

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PluginRepositoryImpl.class);

    /**
     * name of the available directory
     */
    private static final String AVAILABLE = "available";

    /**
     * name of the staged directory
     */
    private static final String STAGED = "staged";

    /**
     * name of the installed directory
     */
    private static final String INSTALLED = "installed";

    /**
     * name of the directory used to install plugins
     */
    private static final String TO_INSTALL = "to-install";

    /**
     * name of the directory used to uninstall plugins
     */
    private static final String TO_UNINSTALL = "to-uninstall";

    /**
     * Root folder of the plugins folder inside Che
     */
    private final Path chePluginsHome;

    /**
     * Root folder of the templates folder inside Che
     */
    private final Path cheTemplatesHome;

    /**
     * Root folder of the machines folder inside Che
     */
    private final Path cheMachinesHome;

    /**
     * Root folder where extensions are stored
     */
    private final Path cheExtFolder;

    /**
     * Path to the available folder
     */
    private final Path availablePluginsFolder;

    /**
     * Path to the staged folder
     */
    private final Path stagedPluginsFolder;

    /**
     * Path to the folder where plugins that are installed are stored
     */
    private final Path installedPluginsFolder;

    /**
     * Path to the folder where we're staging plugins to be installed
     */
    private final Path stagedPluginsToInstallFolder;

    /**
     * Path to the folder where we're staging plugins to be uninstalled
     */
    private final Path stagedPluginsToUninstallFolder;

    @Inject
    public PluginRepositoryImpl(final PluginConfiguration pluginConfiguration) throws PluginRepositoryException {
        this.chePluginsHome = pluginConfiguration.getPluginRootFolder();
        this.cheExtFolder = pluginConfiguration.getExtRootFolder();
        this.cheTemplatesHome = pluginConfiguration.getTemplatesRootFolder();
        this.cheMachinesHome = pluginConfiguration.getMachinesRootFolder();
        this.availablePluginsFolder = chePluginsHome.resolve(AVAILABLE);
        this.stagedPluginsFolder = chePluginsHome.resolve(STAGED);
        this.stagedPluginsToInstallFolder = stagedPluginsFolder.resolve(TO_INSTALL);
        this.stagedPluginsToUninstallFolder = stagedPluginsFolder.resolve(TO_UNINSTALL);
        this.installedPluginsFolder = chePluginsHome.resolve(INSTALLED);

        // create all directories if missing
        try {
            Files.createDirectories(this.availablePluginsFolder);
            Files.createDirectories(this.stagedPluginsFolder);
            Files.createDirectories(this.stagedPluginsToInstallFolder);
            Files.createDirectories(this.stagedPluginsToUninstallFolder);
            Files.createDirectories(this.installedPluginsFolder);
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to create plugin directories", e);
        }

    }

    /**
     * @return list of available plugins
     * @throws PluginRepositoryException
     */
    @Override
    public List<Path> getAvailablePlugins() throws PluginRepositoryException {
        return list(availablePluginsFolder);
    }

    /**
     * @return list of plugins that are staged
     * @throws PluginRepositoryException
     */
    @Override
    public List<Path> getStagedInstallPlugins() throws PluginRepositoryException {
        return list(stagedPluginsToInstallFolder);
    }

    /**
     * @return list of plugins that are staged
     * @throws PluginRepositoryException
     */
    @Override
    public List<Path> getStagedUninstallPlugins() throws PluginRepositoryException {
        return list(stagedPluginsToUninstallFolder);
    }

    /**
     * @return list of plugins that are installed
     * @throws PluginRepositoryException
     */
    @Override
    public List<Path> getInstalledPlugins() throws PluginRepositoryException {
        return list(installedPluginsFolder);
    }

    /**
     * List of all subfolder of a given path
     */
    protected List<Path> list(Path folder) throws PluginRepositoryException {
        List<Path> files = new ArrayList<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folder)) {
            for (Path path : directoryStream) {
                files.add(path);
            }
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to list plugins", e);
        }
        return files;

    }

    /**
     * Add a plugin and make it available
     * @throws PluginRepositoryException if can't add
     */
    public Path add(Path localPlugin) throws PluginRepositoryException {
        if (Files.notExists(localPlugin)) {
            throw new PluginRepositoryException("Unable to add a plugin that is not existing");
        }

        Path destPath = availablePluginsFolder.resolve(localPlugin.getFileName());
        // move it
        try {
            Files.move(localPlugin, destPath);
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to move plugin to staged directory");
        }

        return destPath;

    }


    /**
     * Plugin is going to stage directory to be installed
     * @param availablePlugin plugin that may be installed
     */
    public Path stageInstall(Path availablePlugin) throws PluginRepositoryException {
        if (Files.notExists(availablePlugin)) {
            throw new PluginRepositoryException("Unable to move a plugin that is not existing");
        }


        // move it
        Path newLocation = stagedPluginsToInstallFolder.resolve(availablePlugin.getFileName());
        try {
            Files.move(availablePlugin, newLocation);
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to move plugin to staged directory");
        }


        return newLocation;

    }


    /**
     * Cancel plugin is going to stage directory to be installed
     * @param stagedInstallPlugin plugin that may be installed
     */
    public Path undoStageInstall(Path stagedInstallPlugin) throws PluginRepositoryException {
        if (Files.notExists(stagedInstallPlugin)) {
            throw new PluginRepositoryException("Unable to move a plugin that is not existing");
        }


        // move it
        Path newLocation = availablePluginsFolder.resolve(stagedInstallPlugin.getFileName());
        try {
            Files.move(stagedInstallPlugin, newLocation);
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to move plugin to staged directory");
        }


        return newLocation;
    }


    /**
     * Cancel plugin is going to stage directory to be uninstalled
     * @param stagedUninstallPlugin plugin that may be uninstalled
     */
    public Path undoStageUninstall(Path stagedUninstallPlugin) throws PluginRepositoryException {
        if (Files.notExists(stagedUninstallPlugin)) {
            throw new PluginRepositoryException("Unable to move a plugin that is not existing");
        }


        // move it
        Path newLocation = installedPluginsFolder.resolve(stagedUninstallPlugin.getFileName());
        try {
            Files.move(stagedUninstallPlugin, newLocation);
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to move plugin to staged directory");
        }


        return newLocation;
    }




    /**
     * Ask to uninstall the plugin
     * @param installedPlugin an existing installed plugin
     */
    public Path stageUninstall(Path installedPlugin) throws PluginRepositoryException {
        if (Files.notExists(installedPlugin)) {
            throw new PluginRepositoryException("Unable to move a plugin that is not existing");
        }

        // move it
        Path newLocation = stagedPluginsToUninstallFolder.resolve(installedPlugin.getFileName());
        try {
            Files.move(installedPlugin, newLocation);
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to move plugin to available directory");
        }


        return newLocation;

    }

    /**
     * Helper method that install all extensions from a zip file
     * @param zipFile the path to the plugin zip file
     * @throws PluginRepositoryException
     */
    protected void installExtensionsFromZip(Path zipFile) throws PluginRepositoryException {
        Path tmpPath = null;
        try {
            tmpPath = Files.createTempDirectory(chePluginsHome, "tmp");
        } catch (IOException e) {
            throw new PluginRepositoryException(String.format("Unable to analyze the plugin %s", zipFile), e);
        }

        // analyze file and then copy all jars to ext
        try (InputStream is = new FileInputStream(zipFile.toFile())) {
            ZipUtils.unzip(is, tmpPath.toFile());
        } catch (IOException e) {
            throw new PluginRepositoryException(String.format("Unable to analyze the plugin %s", zipFile), e);
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tmpPath)) {
            for (Path path : directoryStream) {
                Path relativePath = tmpPath.relativize(path);
                // For each extension, copy them to ext
                if (EXTENSIONS_DIR.equals(relativePath.getName(0).toString())) {
                    try (DirectoryStream<Path> pluginsDirectoryStream = Files.newDirectoryStream(path)) {
                        for (Path extensionPath : pluginsDirectoryStream) {
                            installExtension(extensionPath);
                        }
                    }
                } else if (TEMPLATES_DIR.equals(relativePath.getName(0).toString())) {
                    try (DirectoryStream<Path> pluginsDirectoryStream = Files.newDirectoryStream(path)) {
                        for (Path templatePath : pluginsDirectoryStream) {
                            installTemplate(templatePath);
                        }
                    }
                } else if (MACHINES_DIR.equals(relativePath.getName(0).toString())) {
                    try (DirectoryStream<Path> pluginsDirectoryStream = Files.newDirectoryStream(path)) {
                        for (Path machinePath : pluginsDirectoryStream) {
                            installMachine(machinePath);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to stage plugin dependencies", e);
        } finally {
            // Delete temp path
            if (!IoUtil.deleteRecursive(tmpPath.toFile())) {
                LOG.error("Unable to delete folder {0}", tmpPath);
            }
        }
    }

    /**
     * Uninstall the given extension
     * @param extensionFile the extension file
     * @throws PluginRepositoryException
     */
    protected void uninstallExtension(Path extensionFile) throws PluginRepositoryException {
        try {
            Files.delete(cheExtFolder.resolve(extensionFile.getFileName()));
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to stage plugin dependencies", e);
        }
    }

    /**
     * Install the given extension
     * @param extensionFile the path to the extension
     * @throws PluginRepositoryException if install fails
     */
    protected void installExtension(Path extensionFile) throws PluginRepositoryException {
        // Copy jar to ext folder
        try {
            Files.copy(extensionFile, cheExtFolder.resolve(extensionFile.getFileName()));
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to stage plugin dependencies", e);
        }
    }

    /**
     * Install the given template
     * @param templatePath the path to the template
     * @throws PluginRepositoryException if install fails
     */
    protected void installTemplate(Path templatePath) throws PluginRepositoryException {
        // Copy template to templates directory
        try {
            Files.copy(templatePath, cheTemplatesHome.resolve(templatePath.getFileName()));
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to stage plugin dependencies", e);
        }
    }

    /**
     * Uninstall the given template
     * @param templatePath the path to the template
     * @throws PluginRepositoryException if install fails
     */
    protected void uninstallTemplate(Path templatePath) throws PluginRepositoryException {
        // Remove template from templates directory
        try {
            Files.delete(cheTemplatesHome.resolve(templatePath.getFileName()));
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to stage plugin dependencies", e);
        }
    }

    /**
     * Install the given machine
     * @param machinePath the path to the machine
     * @throws PluginRepositoryException if uninstall fails
     */
    protected void installMachine(Path machinePath) throws PluginRepositoryException {
        // Copy template to templates directory
        try {
            Files.walkFileTree(machinePath, new DeepCopy(machinePath, cheMachinesHome.resolve(machinePath.getFileName())));
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to stage plugin dependencies", e);
        }

    }

    /**
     * Uninstall the given template
     * @param machinePath the path to the machine
     * @throws PluginRepositoryException if uninstall fails
     */
    protected void uninstallMachine(Path machinePath) throws PluginRepositoryException {
        // Remove template from templates directory
        IoUtil.deleteRecursive(cheMachinesHome.resolve(machinePath.getFileName()).toFile());
    }


    /**
     * Helper method that uninstall all extensions, machines, templates from a zip file
     * @param zipFile the path to the plugin zip file
     * @throws PluginRepositoryException
     */
    protected void uninstallExtensionsFromZip(Path zipFile) throws PluginRepositoryException {
        Path tmpPath = null;
        try {
            tmpPath = Files.createTempDirectory(chePluginsHome, "tmp");
        } catch (IOException e) {
            throw new PluginRepositoryException(String.format("Unable to analyze the plugin %s", zipFile), e);
        }


        // analyze file and then remove them all jars from ext
        try (InputStream is = new FileInputStream(zipFile.toFile())) {
            ZipUtils.unzip(is, tmpPath.toFile());
        } catch (IOException e) {
            throw new PluginRepositoryException(String.format("Unable to analyze the plugin %s", zipFile), e);
        }


        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tmpPath)) {
            for (Path path : directoryStream) {
                Path relativePath = tmpPath.relativize(path);
                if (EXTENSIONS_DIR.equals(relativePath.getName(0).toString())) {
                    // For each extension, move it out from ext
                    try (DirectoryStream<Path> pluginsDirectoryStream = Files.newDirectoryStream(path)) {
                        for (Path extensionPath : pluginsDirectoryStream) {
                            uninstallExtension(extensionPath);
                        }
                    }
                } else if (TEMPLATES_DIR.equals(relativePath.getName(0).toString())) {
                    try (DirectoryStream<Path> templatesDirectoryStream = Files.newDirectoryStream(path)) {
                        for (Path templatePath : templatesDirectoryStream) {
                            uninstallTemplate(templatePath);
                        }
                    }
                } else if (MACHINES_DIR.equals(relativePath.getName(0).toString())) {
                    try (DirectoryStream<Path> machinesDirectoryStream = Files.newDirectoryStream(path)) {
                        for (Path machinePath : machinesDirectoryStream) {
                            uninstallMachine(machinePath);
                        }
                    }
                }

            }
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to move to available plugin dependencies", e);
        } finally {
            // Delete temp path
            if (!IoUtil.deleteRecursive(tmpPath.toFile())) {
                LOG.error("Unable to delete folder {0}", tmpPath);
            }
        }
    }


    /**
     * Called before staging operation
     * @throws PluginRepositoryException
     */
    public void preStaged() throws PluginRepositoryException {


        // copy all ext files from staged install plugins
        try (DirectoryStream<Path> stagedToInstallDirectoryStream = Files.newDirectoryStream(stagedPluginsToInstallFolder)) {
            for (Path stagedPluginPath : stagedToInstallDirectoryStream) {
                // if zip
                if (getFileName(stagedPluginPath).endsWith(".zip")) {
                    installExtensionsFromZip(stagedPluginPath);
                } else {
                    installExtension(stagedPluginPath);
                }
            }
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to process staged folder", e);
        }


        // remove all ext files from staged uninstall plugins
        try (DirectoryStream<Path> stagedToUninstallDirectoryStream = Files.newDirectoryStream(stagedPluginsToUninstallFolder)) {
            for (Path stagedPluginPath : stagedToUninstallDirectoryStream) {
                // if zip
                if (getFileName(stagedPluginPath).endsWith(".zip")) {
                    // uninstall extensions from zip
                    uninstallExtensionsFromZip(stagedPluginPath);
                } else {
                    // Remove jar from ext folder
                    uninstallExtension(stagedPluginPath);
                }
            }
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to process staged folder", e);
        }
    }


    /**
     * Remove an existing plugin from the available folder
     * @param availablePlugin path to the plugin
     * @throws PluginRepositoryException
     */
    public void remove(Path availablePlugin) throws PluginRepositoryException {
        if (Files.notExists(availablePlugin)) {
            throw new PluginRepositoryException("Unable to remove a plugin that is not existing");
        }

        try {
            Files.delete(availablePlugin);
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to remove plugin", e);
        }
    }


    /**
     * Move all staged plugins to installed or available
     */
    public void stagedComplete() throws PluginRepositoryException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(stagedPluginsToInstallFolder)) {
            for (Path stagedToInstallPlugin : directoryStream) {
                Files.move(stagedToInstallPlugin, installedPluginsFolder.resolve(stagedToInstallPlugin.getFileName()));
            }
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to stage plugin dependencies", e);
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(stagedPluginsToUninstallFolder)) {
            for (Path stagedToUninstallPlugin : directoryStream) {
                Files.move(stagedToUninstallPlugin, availablePluginsFolder.resolve(stagedToUninstallPlugin.getFileName()));
            }
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to stage plugin dependencies", e);
        }


    }

    /**
     * Callback to call when stage operation has failed. It will cancel all current operations
     *
     * @throws PluginRepositoryException
     */
    @Override
    public void stagedFailed() throws PluginRepositoryException {

        // Remove all ext files from staged install plugins
        try (DirectoryStream<Path> stagedToInstallDirectoryStream = Files.newDirectoryStream(stagedPluginsToInstallFolder)) {
            for (Path stagedPluginPath : stagedToInstallDirectoryStream) {
                if (getFileName(stagedPluginPath).endsWith(".zip")) {
                    uninstallExtensionsFromZip(stagedPluginPath);
                } else {
                    uninstallExtension(stagedPluginPath);
                }
            }
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to process staged folder", e);
        }


        // add back all ext files from staged uninstall plugins
        try (DirectoryStream<Path> stagedToUninstallDirectoryStream = Files.newDirectoryStream(stagedPluginsToUninstallFolder)) {
            for (Path stagedPluginPath : stagedToUninstallDirectoryStream) {
                if (getFileName(stagedPluginPath).endsWith(".zip")) {
                    installExtensionsFromZip(stagedPluginPath);
                } else {
                    installExtension(stagedPluginPath);
                }
            }
        } catch (IOException e) {
            throw new PluginRepositoryException("Unable to process staged folder", e);
        }


    }

    /**
     * Helper method allowing to get name of the file from a path
     * @param file the given path
     * @return the simple filename
     */
    protected String getFileName(Path file) {
        String name = "";
        if (file != null) {
            Path path = file.getFileName();
            if (path != null) {
                return path.toString();
            }
        }
        return name;

    }


    /**
     * Perform a copy of folders including files from the folder.
     * @author Florent Benoit
     */
    static class DeepCopy extends SimpleFileVisitor<Path> {
        private final Path source;
        private final Path dest;

        public DeepCopy(Path source, Path dest) {
            this.source = source;
            this.dest = dest;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
            Path targetPath = dest.resolve(source.relativize(path));
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
            Files.copy(path, dest.resolve(source.relativize(path)));
            return CONTINUE;
        }
    }

}
