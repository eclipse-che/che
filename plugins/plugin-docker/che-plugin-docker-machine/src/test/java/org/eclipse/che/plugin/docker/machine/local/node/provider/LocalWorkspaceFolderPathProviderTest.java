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
package org.eclipse.che.plugin.docker.machine.local.node.provider;

import com.google.inject.Provider;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.plugin.docker.machine.WindowsHostUtils;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Listeners(value = {MockitoTestNGListener.class})
public class LocalWorkspaceFolderPathProviderTest  {
    private static final String WS_ID   = "testWsId";
    private static final String WS_NAME = "testWsName";

    @Mock
    private WorkspaceManager            workspaceManager;

    private Provider<WorkspaceManager> workspaceManagerProvider = new TestProvider();

    private String singleFolderForAllWorkspaces;
    private String oldWorkspacesRoot;
    private File workspacesRootFile;
    private String workspacesRoot;

    @BeforeMethod
    public void setUp() throws Exception {
        WorkspaceImpl workspace = mock(WorkspaceImpl.class);
        WorkspaceConfigImpl workspaceConfig = mock(WorkspaceConfigImpl.class);
        when(workspaceManager.getWorkspace(WS_ID)).thenReturn(workspace);
        when(workspace.getConfig()).thenReturn(workspaceConfig);
        when(workspaceConfig.getName()).thenReturn(WS_NAME);

        Path tempDirectory = Files.createTempDirectory(getClass().getSimpleName());
        workspacesRoot = tempDirectory.toString();
        workspacesRootFile = tempDirectory.toFile();
        singleFolderForAllWorkspaces = Paths.get(workspacesRoot, "singleFolderForAllWorkspaces").toString();
        oldWorkspacesRoot = Paths.get(workspacesRoot, "oldWorkspacesRoot").toString();
    }

    @Test
    public void createsFoldersByDefault() throws Exception {
        assertTrue(workspacesRootFile.delete());

        LocalWorkspaceFolderPathProvider provider = new LocalWorkspaceFolderPathProvider(workspacesRoot,
                                                                                         workspaceManagerProvider);

        provider.init();

        assertTrue(workspacesRootFile.exists());
        assertTrue(workspacesRootFile.isDirectory());

        String providerPath = provider.getPath(WS_ID);

        assertTrue(new File(providerPath).exists());
        assertTrue(new File(providerPath).isDirectory());
    }

    @Test
    public void returnSpecificFolderOnOnWindows() throws Exception {
        LocalWorkspaceFolderPathProvider provider = new LocalWorkspaceFolderPathProvider(workspacesRoot,
                                                                                         oldWorkspacesRoot,
                                                                                         singleFolderForAllWorkspaces,
                                                                                         workspaceManagerProvider,
                                                                                         false,// with true can not be tested on other OSes
                                                                                         true);

        provider.init();
        String providerPath = provider.getPath(WS_ID);

        assertEquals(providerPath, WindowsHostUtils.getCheHome().resolve("vfs").resolve(WS_NAME).toString());
    }

    @Test
    public void returnsSingleFolderForAllWorkspacesIfConfigured() throws Exception {
        LocalWorkspaceFolderPathProvider provider = new LocalWorkspaceFolderPathProvider(workspacesRoot,
                                                                                         oldWorkspacesRoot,
                                                                                         singleFolderForAllWorkspaces,
                                                                                         workspaceManagerProvider,
                                                                                         false,
                                                                                         false);

        provider.init();
        String providerPath = provider.getPath(WS_ID);

        assertEquals(providerPath, singleFolderForAllWorkspaces);
    }

    @Test
    public void useOlderFolderIfConfigured() throws Exception {
        LocalWorkspaceFolderPathProvider provider = new LocalWorkspaceFolderPathProvider(workspacesRoot,
                                                                                         oldWorkspacesRoot,
                                                                                         null,
                                                                                         workspaceManagerProvider,
                                                                                         false,
                                                                                         false);

        provider.init();
        String providerPath = provider.getPath(WS_ID);

        assertEquals(providerPath, Paths.get(oldWorkspacesRoot, WS_NAME).toString());
    }

    @Test
    public void neitherCheckNorCreateFoldersIfCreationIsDisabled() throws Exception {
        assertTrue(workspacesRootFile.delete());
        LocalWorkspaceFolderPathProvider provider = new LocalWorkspaceFolderPathProvider(workspacesRoot,
                                                                                         null,
                                                                                         null,
                                                                                         workspaceManagerProvider,
                                                                                         false,
                                                                                         false);

        provider.init();
        String providerPath = provider.getPath(WS_ID);

        assertFalse(workspacesRootFile.exists());
        assertFalse(new File(providerPath).exists());
    }

    @Test
    public void createsFoldersIfConfigured() throws Exception {
        assertTrue(workspacesRootFile.delete());
        LocalWorkspaceFolderPathProvider provider = new LocalWorkspaceFolderPathProvider(workspacesRoot,
                                                                                         null,
                                                                                         null,
                                                                                         workspaceManagerProvider,
                                                                                         true,
                                                                                         false);

        provider.init();

        assertTrue(workspacesRootFile.exists());
        assertTrue(workspacesRootFile.isDirectory());

        String providerPath = provider.getPath(WS_ID);

        assertTrue(new File(providerPath).exists());
        assertTrue(new File(providerPath).isDirectory());
    }

    @Test
    public void worksIfWorkspaceFolderExists() throws Exception {
        assertTrue(Paths.get(workspacesRoot, WS_NAME).toFile().mkdir());
        LocalWorkspaceFolderPathProvider provider = new LocalWorkspaceFolderPathProvider(workspacesRoot,
                                                                                         null,
                                                                                         null,
                                                                                         workspaceManagerProvider,
                                                                                         true,
                                                                                         false);

        provider.init();

        assertTrue(workspacesRootFile.exists());
        assertTrue(workspacesRootFile.isDirectory());

        String providerPath = provider.getPath(WS_ID);

        assertTrue(new File(providerPath).exists());
        assertTrue(new File(providerPath).isDirectory());
    }

    @Test(expectedExceptions = IOException.class,
          expectedExceptionsMessageRegExp = "Workspace folder '.*' is not directory")
    public void throwsExceptionIfFileIsFoundByWorkspacesPath() throws Exception {
        assertTrue(Paths.get(workspacesRoot, WS_NAME).toFile().createNewFile());
        LocalWorkspaceFolderPathProvider provider = new LocalWorkspaceFolderPathProvider(workspacesRoot,
                                                                                         null,
                                                                                         null,
                                                                                         workspaceManagerProvider,
                                                                                         true,
                                                                                         false);

        provider.init();
        provider.getPath(WS_ID);
    }

    @Test(expectedExceptions = IOException.class,
          expectedExceptionsMessageRegExp = "Workspace folder '.*' is not directory. Check .* configuration property")
    public void throwsExceptionIfFileIsFoundByWorkspacesRootPath() throws Exception {
        Path tempFile = Files.createTempFile(getClass().getSimpleName(), null);
        LocalWorkspaceFolderPathProvider provider = new LocalWorkspaceFolderPathProvider(tempFile.toString(),
                                                                                         null,
                                                                                         null,
                                                                                         workspaceManagerProvider,
                                                                                         true,
                                                                                         false);

        provider.init();
    }

    @Test(expectedExceptions = IOException.class,
          expectedExceptionsMessageRegExp = "Workspace folder '.*' is not directory. Check .* configuration property")
    public void throwsExceptionIfFileIsFoundBySingleWorkspacePath() throws Exception {
        Path tempFile = Files.createTempFile(getClass().getSimpleName(), null);
        LocalWorkspaceFolderPathProvider provider = new LocalWorkspaceFolderPathProvider(workspacesRoot,
                                                                                         oldWorkspacesRoot,
                                                                                         tempFile.toString(),
                                                                                         workspaceManagerProvider,
                                                                                         true,
                                                                                         false);

        provider.init();
        provider.getPath(WS_ID);
    }

    @Test(expectedExceptions = IOException.class,
          expectedExceptionsMessageRegExp = "expected test exception")
    public void throwsIOExceptionIfWorkspaceRetrievalFails() throws Exception {
        when(workspaceManager.getWorkspace(WS_ID)).thenThrow(new ServerException("expected test exception"));
        LocalWorkspaceFolderPathProvider provider = new LocalWorkspaceFolderPathProvider(workspacesRoot,
                                                                                         null,
                                                                                         null,
                                                                                         workspaceManagerProvider,
                                                                                         true,
                                                                                         false);

        provider.init();
        provider.getPath(WS_ID);
    }

    private class TestProvider implements Provider<WorkspaceManager> {
        @Override
        public WorkspaceManager get() {
            return workspaceManager;
        }
    }
}
