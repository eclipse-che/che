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

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;

import org.eclipse.che.plugin.internal.api.DtoBuilder;
import org.eclipse.che.plugin.internal.api.IPlugin;
import org.eclipse.che.plugin.internal.api.IPluginInstall;
import org.eclipse.che.plugin.internal.api.PluginResolver;
import org.eclipse.che.plugin.internal.api.PluginInstaller;
import org.eclipse.che.plugin.internal.api.PluginInstallerException;
import org.eclipse.che.plugin.internal.api.PluginInstallerNotFoundException;
import org.eclipse.che.plugin.internal.api.PluginManager;
import org.eclipse.che.plugin.internal.api.PluginManagerException;
import org.eclipse.che.plugin.internal.api.PluginManagerNotFoundException;
import org.eclipse.che.plugin.internal.api.PluginRepository;
import org.eclipse.che.plugin.internal.api.PluginRepositoryException;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.plugin.internal.api.IPluginAction.TO_INSTALL;
import static org.eclipse.che.plugin.internal.api.IPluginAction.TO_UNINSTALL;
import static org.eclipse.che.plugin.internal.api.IPluginAction.UNDO_TO_INSTALL;
import static org.eclipse.che.plugin.internal.api.IPluginAction.UNDO_TO_UNINSTALL;
import static org.eclipse.che.plugin.internal.api.IPluginCategory.USER;
import static org.eclipse.che.plugin.internal.api.IPluginStatus.AVAILABLE;
import static org.eclipse.che.plugin.internal.api.IPluginStatus.INSTALLED;
import static org.eclipse.che.plugin.internal.api.IPluginStatus.REMOVED;
import static org.eclipse.che.plugin.internal.api.IPluginStatus.STAGED_INSTALL;
import static org.eclipse.che.plugin.internal.api.IPluginStatus.STAGED_UNINSTALL;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class PluginManagerImplTest {

    @Mock
    private PluginRepository pluginRepository;

    @Mock
    private PluginResolver pluginResolver;

    @Mock
    private PluginInstaller pluginInstaller;

    @Mock
    private DtoBuilder dtoBuilder;


    public PluginManagerImpl getPluginManager() throws PluginRepositoryException {
        return new PluginManagerImpl(pluginRepository, dtoBuilder, Sets.newHashSet(pluginResolver), pluginInstaller);
    }

    @Test
    public void testListPlugins() throws PluginRepositoryException, IOException {
        // add plugin
        Path dummyPath = Files.createTempFile("dummy", "jar");
        doReturn(Arrays.asList(dummyPath)).when(pluginRepository).getAvailablePlugins();

        List<IPlugin> plugins = getPluginManager().listPlugins();
        assertEquals(plugins.size(), 1);
        IPlugin plugin = plugins.get(0);
        assertEquals(plugin.getCategory(), USER);
        assertEquals(plugin.getStatus(), AVAILABLE);
        assertEquals(plugin.getName(), dummyPath.getFileName().toString());

        Files.delete(dummyPath);
    }

    @Test
    public void addMavenPlugin() throws Exception {

        String reference = "mvn:groupId:artifactId";


        Path dummyPath = Files.createTempFile("dummy", "jar");
        doReturn(dummyPath).when(pluginResolver).download(reference);
        doReturn("mvn:").when(pluginResolver).getProtocol();

        // return path when adding
        Path availablePath = Files.createTempFile("dummy", "jar");
        doReturn(availablePath).when(pluginRepository).add(dummyPath);

        // get manager
        PluginManager pluginManager = getPluginManager();

        // call operation
        IPlugin registeredPlugin = pluginManager.addPlugin("mvn:groupId:artifactId");

        // check download manager has been called
        verify(pluginResolver).getProtocol();
        verify(pluginResolver).download(reference);

        // check add is called on plugin repository
        verify(pluginRepository).add(dummyPath);


        assertEquals(registeredPlugin.getCategory(), USER);
        assertEquals(registeredPlugin.getStatus(), AVAILABLE);
        assertEquals(registeredPlugin.getName(), availablePath.getFileName().toString());


        // cleanup
        Files.delete(dummyPath);
        Files.delete(availablePath);

    }


    @Test
    public void testGetPluginDetails() throws PluginRepositoryException, IOException, PluginManagerNotFoundException {
        // add plugin
        Path dummyPath = Files.createTempFile("dummy", "jar");
        String pluginName = dummyPath.getFileName().toString();
        doReturn(Arrays.asList(dummyPath)).when(pluginRepository).getAvailablePlugins();

        IPlugin plugin = getPluginManager().getPluginDetails(pluginName);
        assertNotNull(plugin);
        assertEquals(plugin.getStatus(), AVAILABLE);
        assertEquals(plugin.getCategory(), USER);

    }


    @Test(expectedExceptions = PluginManagerNotFoundException.class)
    public void testGetPluginDetailsNotFound() throws PluginRepositoryException, IOException, PluginManagerNotFoundException {
        getPluginManager().getPluginDetails("not found");
    }


    @Test
    public void testRemovePlugin() throws PluginRepositoryException, IOException, PluginManagerException {
        // add plugin
        Path dummyPath = Files.createTempFile("dummy", "jar");
        String pluginName = dummyPath.getFileName().toString();
        doReturn(Arrays.asList(dummyPath)).when(pluginRepository).getAvailablePlugins();

        IPlugin plugin = getPluginManager().removePlugin(pluginName);
        assertNotNull(plugin);
        assertEquals(plugin.getStatus(), REMOVED);
        assertEquals(plugin.getCategory(), USER);

        // check removed on repository
        verify(pluginRepository).remove(dummyPath);

    }


    @Test
    public void testUpdatePluginToInstall() throws PluginRepositoryException, IOException, PluginManagerException {
        // add plugin
        Path dummyPath = Files.createTempFile("dummy", "jar");
        String pluginName = dummyPath.getFileName().toString();
        doReturn(Arrays.asList(dummyPath)).when(pluginRepository).getAvailablePlugins();

        IPlugin plugin = getPluginManager().updatePlugin(pluginName, TO_INSTALL);
        assertNotNull(plugin);
        assertEquals(plugin.getStatus(), STAGED_INSTALL);
        assertEquals(plugin.getCategory(), USER);

        // check we've called staged
        verify(pluginRepository).stageInstall(dummyPath);
    }

    @Test(expectedExceptions = PluginManagerException.class)
    public void testUpdatePluginToInstallInvalid() throws PluginRepositoryException, IOException, PluginManagerException {
        // add plugin
        Path dummyPath = Files.createTempFile("dummy", "jar");
        String pluginName = dummyPath.getFileName().toString();
        doReturn(Arrays.asList(dummyPath)).when(pluginRepository).getInstalledPlugins();

        getPluginManager().updatePlugin(pluginName, TO_INSTALL);
    }

    @Test
    public void testUpdatePluginUndoToInstall() throws PluginRepositoryException, IOException, PluginManagerException {
        // add plugin
        Path dummyPath = Files.createTempFile("dummy", "jar");
        String pluginName = dummyPath.getFileName().toString();
        doReturn(Arrays.asList(dummyPath)).when(pluginRepository).getStagedInstallPlugins();

        IPlugin plugin = getPluginManager().updatePlugin(pluginName, UNDO_TO_INSTALL);
        assertNotNull(plugin);
        assertEquals(plugin.getStatus(), AVAILABLE);
        assertEquals(plugin.getCategory(), USER);

        // check we've called staged
        verify(pluginRepository).undoStageInstall(dummyPath);
    }

    @Test(expectedExceptions = PluginManagerException.class)
    public void testUpdatePluginUndoToInstallInvalid() throws PluginRepositoryException, IOException, PluginManagerException {
        // add plugin
        Path dummyPath = Files.createTempFile("dummy", "jar");
        String pluginName = dummyPath.getFileName().toString();
        doReturn(Arrays.asList(dummyPath)).when(pluginRepository).getInstalledPlugins();

        getPluginManager().updatePlugin(pluginName, UNDO_TO_INSTALL);
    }


    @Test
    public void testUpdatePluginToUninstall() throws PluginRepositoryException, IOException, PluginManagerException {
        // add plugin
        Path dummyPath = Files.createTempFile("dummy", "jar");
        String pluginName = dummyPath.getFileName().toString();
        doReturn(Arrays.asList(dummyPath)).when(pluginRepository).getInstalledPlugins();

        IPlugin plugin = getPluginManager().updatePlugin(pluginName, TO_UNINSTALL);
        assertNotNull(plugin);
        assertEquals(plugin.getStatus(), STAGED_UNINSTALL);
        assertEquals(plugin.getCategory(), USER);

        // check we've called staged
        verify(pluginRepository).stageUninstall(dummyPath);
    }

    @Test(expectedExceptions = PluginManagerException.class)
    public void testUpdatePluginToUninstallInvalid() throws PluginRepositoryException, IOException, PluginManagerException {
        // add plugin
        Path dummyPath = Files.createTempFile("dummy", "jar");
        String pluginName = dummyPath.getFileName().toString();
        doReturn(Arrays.asList(dummyPath)).when(pluginRepository).getAvailablePlugins();

        getPluginManager().updatePlugin(pluginName, TO_UNINSTALL);
    }


    @Test
    public void testUpdatePluginUndoToUninstall() throws PluginRepositoryException, IOException, PluginManagerException {
        // add plugin
        Path dummyPath = Files.createTempFile("dummy", "jar");
        String pluginName = dummyPath.getFileName().toString();
        doReturn(Arrays.asList(dummyPath)).when(pluginRepository).getStagedUninstallPlugins();

        IPlugin plugin = getPluginManager().updatePlugin(pluginName, UNDO_TO_UNINSTALL);
        assertNotNull(plugin);
        assertEquals(plugin.getStatus(), INSTALLED);
        assertEquals(plugin.getCategory(), USER);

        // check we've called staged
        verify(pluginRepository).undoStageUninstall(dummyPath);
    }


    @Test(expectedExceptions = PluginManagerException.class)
    public void testUpdatePluginUndoToUninstallInvalid() throws PluginRepositoryException, IOException, PluginManagerException {
        // add plugin
        Path dummyPath = Files.createTempFile("dummy", "jar");
        String pluginName = dummyPath.getFileName().toString();
        doReturn(Arrays.asList(dummyPath)).when(pluginRepository).getAvailablePlugins();

        getPluginManager().updatePlugin(pluginName, UNDO_TO_UNINSTALL);
    }


    @Test
    public void testListInstall() throws PluginRepositoryException, PluginManagerException {

        List<IPluginInstall> pluginInstalls = new ArrayList<>();
        IPluginInstall pluginInstallTest = mock(IPluginInstall.class);
        pluginInstalls.add(pluginInstallTest);
        doReturn(pluginInstalls).when(pluginInstaller).listInstall();

        List<IPluginInstall> checkInstalls = getPluginManager().listInstall();
        assertEquals(checkInstalls, pluginInstalls);
        verify(pluginInstaller).listInstall();

    }


    @Test
    public void testRequireNewInstallSuccess() throws PluginRepositoryException, PluginManagerException, PluginInstallerException {
        PluginManager pluginManager = getPluginManager();

        long testID = 1L;
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            FutureCallback callback = (FutureCallback) args[0];
            // exitCode 0
            callback.onSuccess(0);
            IPluginInstall mockInstall = mock(IPluginInstall.class);
            doReturn(testID).when(mockInstall).getId();

            return mockInstall;
        }).when(pluginInstaller).requireNewInstall(any(FutureCallback.class));


        // call
        IPluginInstall pluginInstall = pluginManager.requireNewInstall();


        // checks
        verify(pluginRepository).preStaged();
        verify(pluginRepository).stagedComplete();
        verify(pluginRepository, never()).stagedFailed();
        assertEquals(pluginInstall.getId(), testID);
    }


    @Test
    public void testRequireNewInstallFailure() throws PluginRepositoryException, PluginManagerException, PluginInstallerException {
        PluginManager pluginManager = getPluginManager();

        long testID = 1L;
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            FutureCallback callback = (FutureCallback) args[0];
            callback.onFailure(null);
            IPluginInstall mockInstall = mock(IPluginInstall.class);
            doReturn(testID).when(mockInstall).getId();

            return mockInstall;
        }).when(pluginInstaller).requireNewInstall(any(FutureCallback.class));


        // call
        IPluginInstall pluginInstall = pluginManager.requireNewInstall();


        // checks
        verify(pluginRepository).preStaged();
        verify(pluginRepository).stagedFailed();
        verify(pluginRepository, never()).stagedComplete();
        assertEquals(pluginInstall.getId(), testID);
    }


    @Test
    public void testGetInstallSuccess() throws PluginRepositoryException, PluginManagerException, PluginInstallerNotFoundException {
        PluginManager pluginManager = getPluginManager();
        long testID = 1L;
        IPluginInstall mockInstall = mock(IPluginInstall.class);
        doReturn(testID).when(mockInstall).getId();
        doReturn(mockInstall).when(pluginInstaller).getInstall(eq(testID));

        IPluginInstall install = pluginManager.getInstall(testID);
        assertEquals(install.getId(), testID);

    }

}