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
package org.eclipse.che.plugin.internal.manager;


import com.google.common.util.concurrent.FutureCallback;

import org.eclipse.che.plugin.internal.Plugin;
import org.eclipse.che.plugin.internal.api.DtoBuilder;
import org.eclipse.che.plugin.internal.api.IPlugin;
import org.eclipse.che.plugin.internal.api.IPluginAction;
import org.eclipse.che.plugin.internal.api.IPluginCategory;
import org.eclipse.che.plugin.internal.api.IPluginInstall;
import org.eclipse.che.plugin.internal.api.IPluginStatus;
import org.eclipse.che.plugin.internal.api.PluginInstaller;
import org.eclipse.che.plugin.internal.api.PluginInstallerException;
import org.eclipse.che.plugin.internal.api.PluginInstallerNotFoundException;
import org.eclipse.che.plugin.internal.api.PluginManager;
import org.eclipse.che.plugin.internal.api.PluginManagerAlreadyExistsException;
import org.eclipse.che.plugin.internal.api.PluginManagerException;
import org.eclipse.che.plugin.internal.api.PluginManagerNotFoundException;
import org.eclipse.che.plugin.internal.api.PluginRepository;
import org.eclipse.che.plugin.internal.api.PluginRepositoryException;
import org.eclipse.che.plugin.internal.api.PluginResolver;
import org.eclipse.che.plugin.internal.api.PluginResolverException;
import org.eclipse.che.plugin.internal.api.PluginResolverNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.eclipse.che.plugin.internal.api.IPluginCategory.USER;
import static org.eclipse.che.plugin.internal.api.IPluginStatus.AVAILABLE;
import static org.eclipse.che.plugin.internal.api.IPluginStatus.INSTALLED;
import static org.eclipse.che.plugin.internal.api.IPluginStatus.REMOVED;
import static org.eclipse.che.plugin.internal.api.IPluginStatus.STAGED_INSTALL;
import static org.eclipse.che.plugin.internal.api.IPluginStatus.STAGED_UNINSTALL;

/**
 * @author Florent Benoit
 */
@Singleton
public class PluginManagerImpl implements PluginManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginManagerImpl.class);


    private final DtoBuilder       dtoBuilder;
    private final PluginRepository pluginRepository;

    private final Set<PluginResolver> downloaders;

    private PluginInstaller pluginInstaller;

    private Map<String, Plugin> pluginDescriptors = new HashMap<>();


    @Inject
    public PluginManagerImpl(final PluginRepository pluginRepository, final DtoBuilder dtoBuilder,
                             final Set<PluginResolver> downloaders, PluginInstaller pluginInstaller)
            throws PluginRepositoryException {
        this.pluginRepository = pluginRepository;
        this.dtoBuilder = dtoBuilder;
        this.downloaders = downloaders;
        this.pluginInstaller = pluginInstaller;

        this.pluginDescriptors = new ConcurrentHashMap<>();
        initPlugins();
    }

    protected void initPlugins() throws PluginRepositoryException {

        this.pluginDescriptors.clear();

        // add available plugins
        pluginDescriptors.putAll(pluginRepository.getAvailablePlugins().stream()
                                                 .map(plugin -> buildPlugin(plugin, AVAILABLE, USER))
                                                 .collect(Collectors.toMap(Plugin::getName,
                                                                           plugin -> plugin)));
        // add staged plugins
        pluginDescriptors.putAll(pluginRepository.getStagedInstallPlugins().stream()
                                                 .map(plugin -> buildPlugin(plugin, STAGED_INSTALL, USER))
                                                 .collect(Collectors.toMap(Plugin::getName,
                                                                           plugin -> plugin)));
        pluginDescriptors.putAll(pluginRepository.getStagedUninstallPlugins().stream()
                                                 .map(plugin -> buildPlugin(plugin, STAGED_UNINSTALL, USER))
                                                 .collect(Collectors.toMap(Plugin::getName,
                                                                           plugin -> plugin)));
        // add installed plugins
        pluginDescriptors.putAll(pluginRepository.getInstalledPlugins().stream()
                                                 .map(plugin -> buildPlugin(plugin, INSTALLED, USER))
                                                 .collect(Collectors.toMap(Plugin::getName,
                                                                           plugin -> plugin)));
    }


    Plugin buildPlugin(Path pluginFile, IPluginStatus status, IPluginCategory category) {
        Plugin plugin = new Plugin();


        if (pluginFile != null) {
            Path path = pluginFile.getFileName();
            if (path != null) {
                String name = path.toString();
                plugin.setName(name);

                if (name.endsWith(".jar")) {
                    // read version
                    try (FileInputStream fis = new FileInputStream(pluginFile.toFile()); JarInputStream jarStream = new JarInputStream(fis)) {
                        Manifest mf = jarStream.getManifest();
                        if (mf != null) {
                            String version = mf.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
                            if (version != null) {
                                plugin.setVersion(version);
                            }
                        }
                    } catch (IOException e) {
                        LOGGER.error(format("Unable to get version from %s", pluginFile));
                    }

                } else if (name.endsWith(".zip")) {
                    // read descriptor
                    URI uri = URI.create("jar:" + pluginFile.toFile().toURI());
                    try (FileSystem zipfs = FileSystems.newFileSystem(uri, new HashMap<String, String>())) {
                        Path pluginDescriptorPath = zipfs.getPath("/che-plugin.yml");
                        if (pluginDescriptorPath != null) {

                            List<String> lines = Files.readAllLines(pluginDescriptorPath);
                            Optional<String> optionalVersion = lines.stream().filter((line) -> (line.startsWith("version:")))
                                    .map((line) -> (line.substring("version:".length())))
                                    .findFirst();

                            if (optionalVersion.get() != null) {
                                plugin.setVersion(optionalVersion.get());
                            }

                        }
                    } catch (IOException e) {
                        LOGGER.error(format("Unable to get version from %s", pluginFile));
                    }

                }

            }
        }


        return plugin.setStatus(status).setCategory(category).setPath(pluginFile);
    }


    /**
     * @return list of all plugins managed by the system
     */
    @Override
    public List<IPlugin> listPlugins() throws PluginRepositoryException {
        return new ArrayList<>(pluginDescriptors.values());
    }

    /**
     * Gets the list of all install
     * @return the IDs of every install
     */
    @Override
    public List<IPluginInstall> listInstall() throws PluginManagerException {
        return pluginInstaller.listInstall();
    }

    @Override
    public IPluginInstall requireNewInstall() throws PluginManagerException, PluginInstallerException {


        // first, prepare stage
        try {
            this.pluginRepository.preStaged();
        } catch (PluginRepositoryException e) {
            throw new PluginManagerException("Unable to prepare the staging operation", e);
        }


        return this.pluginInstaller.requireNewInstall(new FutureCallback<Integer>() {
            @Override
            public void onSuccess(Integer exitCode) {

                if (exitCode != null && 0 == exitCode) {
                    // success
                    try {
                        pluginRepository.stagedComplete();
                    } catch (PluginRepositoryException e) {
                        LOGGER.error("Unable to do post-install action", e);
                    }
                } else {
                    // failed
                    try {
                        pluginRepository.stagedFailed();
                    } catch (PluginRepositoryException e) {
                        LOGGER.error("Unable to do post-install action", e);
                    }
                }
                try {
                    initPlugins();
                } catch (PluginRepositoryException e) {
                    LOGGER.error("Unable to init plugins", e);
                }


            }

            @Override
            public void onFailure(Throwable throwable) {
                try {
                    pluginRepository.stagedFailed();
                } catch (PluginRepositoryException e) {
                    LOGGER.error("Unable to do post-install action", e);
                }
                try {
                    initPlugins();
                } catch (PluginRepositoryException e) {
                    LOGGER.error("Unable to init plugins", e);
                }
            }
        });
    }


    public IPlugin addPlugin(final String pluginRef) throws PluginResolverNotFoundException, PluginManagerException {


        // resolve plugin

        if (pluginRef == null) {
            throw new PluginManagerException("Invalid plugin reference");
        }

        // search downloader
        Optional<PluginResolver> matchedDownloader =  downloaders.stream().filter(pluginDownloader -> pluginRef
                .startsWith(pluginDownloader.getProtocol())).findFirst();


        if (!matchedDownloader.isPresent()) {
            throw new PluginManagerException(format("No downloader for reference %s", pluginRef));
        }

        Path toInstallpluginPath;
        try {
            toInstallpluginPath = matchedDownloader.get().download(pluginRef);
        } catch (PluginResolverException e) {
            throw new PluginManagerException(format("Unable to get reference %s", pluginRef), e);
        }


        // TODO: validate plugin


        // plugin validated, add it to available plugin
        Path availablePluginPath;
        try {
            availablePluginPath = this.pluginRepository.add(toInstallpluginPath);
        } catch (PluginRepositoryException e) {
            throw new PluginManagerException(format("Unable to add given plugin %s", pluginRef), e);
        }

        Plugin plugin = buildPlugin(availablePluginPath, AVAILABLE, USER);

        // check there is none
        if (this.pluginDescriptors.containsKey(plugin.getName())) {
            throw new PluginManagerAlreadyExistsException(format("The plugin %s is already managed.", plugin.getName()));
        }


        this.pluginDescriptors.put(plugin.getName(), plugin);

        return plugin;
    }

    @Override
    public IPlugin updatePlugin(String pluginName, IPluginAction pluginAction) throws PluginManagerException, PluginRepositoryException {
        // get plugin
        Plugin plugin = getPluginDetails(pluginName);

        IPluginStatus currentStatus = plugin.getStatus();

        // update status
        switch (pluginAction) {
            case TO_INSTALL:
                if (AVAILABLE == currentStatus) {
                    Path newPath = this.pluginRepository.stageInstall(plugin.getPath());
                    plugin.setStatus(STAGED_INSTALL);
                    plugin.setPath(newPath);
                } else {
                    throw new PluginManagerException(format("Plugin %s can't be staged to be installed", pluginName));
                }
                break;
            case TO_UNINSTALL:
                if (INSTALLED == currentStatus) {
                    Path newPath = this.pluginRepository.stageUninstall(plugin.getPath());
                    plugin.setStatus(STAGED_UNINSTALL);
                    plugin.setPath(newPath);
                } else {
                    throw new PluginManagerException(format("Plugin %s can't be staged to be uninstalled", pluginName));
                }
                break;
            case UNDO_TO_INSTALL:
                if (STAGED_INSTALL == currentStatus) {
                    Path newPath = this.pluginRepository.undoStageInstall(plugin.getPath());
                    plugin.setStatus(AVAILABLE);
                    plugin.setPath(newPath);
                } else {
                    throw new PluginManagerException(format("Plugin %s can't undo be staged to be installed", pluginName));
                }
                break;
            case UNDO_TO_UNINSTALL:
                if (STAGED_UNINSTALL == currentStatus) {
                    Path newPath = this.pluginRepository.undoStageUninstall(plugin.getPath());
                    plugin.setStatus(INSTALLED);
                    plugin.setPath(newPath);
                } else {
                    throw new PluginManagerException(format("Plugin %s can't undo be staged to be uninstalled", pluginName));
                }
                break;
        }
        return plugin;
    }


    public Plugin getPluginDetails(String pluginName) throws PluginManagerNotFoundException {
        // get plugin
        Plugin plugin = this.pluginDescriptors.get(pluginName);

        if (plugin == null) {
            throw new PluginManagerNotFoundException(format("No plugin found for name %s", pluginName));
        }
        return plugin;
    }

    public Plugin removePlugin(String pluginName) throws PluginManagerException {
        // get plugin
        Plugin plugin = this.pluginDescriptors.get(pluginName);

        if (plugin == null) {
            throw new PluginManagerNotFoundException(format("No plugin found for name %s", pluginName));
        }

        try {
            this.pluginRepository.remove(plugin.getPath());
        } catch (PluginRepositoryException e) {
            throw new PluginManagerException(format("Unable to remove the plugin %s", pluginName));
        }

        // remove it from list
        plugin.setStatus(REMOVED);
        this.pluginDescriptors.remove(pluginName);

        return plugin;

    }


    public IPluginInstall getInstall(long id) throws PluginInstallerNotFoundException {
        return this.pluginInstaller.getInstall(id);
    }



}
