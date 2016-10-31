/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.svn.server;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.importer.ProjectImporter;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.plugin.ssh.key.script.SshKeyProvider;
import org.eclipse.che.plugin.svn.server.repository.RepositoryUrlProvider;
import org.eclipse.che.plugin.svn.server.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubversionProjectImporterTest {

    @Mock
    private ProfileDao userProfileDao;
    @Mock
    private RepositoryUrlProvider repositoryUrlProvider;
    @Mock
    private SourceStorage         sourceStorage;
    @Mock
    private SshKeyProvider        sshKeyProvider;

    private File                      repoRoot;
    private SubversionProjectImporter projectImporter;
    private VirtualFile               root;

    @Before
    public void setUp() throws Exception {
        // Bind components
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                Multibinder.newSetBinder(binder(), ProjectImporter.class).addBinding().to(SubversionProjectImporter.class);
                Multibinder.newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(SubversionProjectType.class);
                Multibinder.newSetBinder(binder(), ValueProviderFactory.class).addBinding()
                           .to(SubversionValueProviderFactory.class);

                bind(SshKeyProvider.class).toInstance(sshKeyProvider);
                bind(ProfileDao.class).toInstance(userProfileDao);
                bind(RepositoryUrlProvider.class).toInstance(repositoryUrlProvider);
            }
        });

        // Init virtual file system
        VirtualFileSystem virtualFileSystem = TestUtils.createVirtualFileSystem();
        root = virtualFileSystem.getRoot();

        // Create the test user
        TestUtils.createTestUser(userProfileDao);

        // Create the Subversion repository
        repoRoot = TestUtils.createGreekTreeRepository();

        projectImporter = injector.getInstance(SubversionProjectImporter.class);
    }

    /**
     * Test for {@link SubversionProjectImporter#getCategory()}.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testGetCategory() throws Exception {
        assertEquals(projectImporter.getCategory(), ProjectImporter.ImporterCategory.SOURCE_CONTROL);
    }

    /**
     * Test for {@link SubversionProjectImporter#getDescription()}.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testGetDescription() throws Exception {
        assertEquals(projectImporter.getDescription(), "Import project from Subversion repository URL.");
    }

    /**
     * Test for {@link SubversionProjectImporter#getId()}
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testGetId() throws Exception {
        assertEquals(projectImporter.getId(), "subversion");
    }

    /**
     * Test for {@link SubversionProjectImporter#isInternal()}.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testIsInternal() throws Exception {
        assertEquals(projectImporter.isInternal(), false);
    }

    /**
     * Test for {@link SubversionProjectImporter#importSources(org.eclipse.che.api.project.server.FolderEntry, org.eclipse.che.api.core.model.project.SourceStorage, org.eclipse.che.api.core.util.LineConsumerFactory)}
     * invalid url.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testInvalidImportSources() throws Exception {
        final String projectName = NameGenerator.generate("project-", 3);
        final VirtualFile virtualFile = root.createFolder(projectName);//root.getChild(org.eclipse.che.api.vfs.Path.of(projectName));
        FolderEntry projectFolder = new FolderEntry(virtualFile);
        try {
            String fakeUrl = Paths.get(repoRoot.getAbsolutePath()).toUri() + "fake";
            when(sourceStorage.getLocation()).thenReturn(fakeUrl);
            projectImporter.importSources(projectFolder, sourceStorage, new TestUtils.SystemOutLineConsumerFactory());

            fail("The code above should had failed");
        } catch (SubversionException e) {
            final String message = e.getMessage();

            boolean assertBoolean = Pattern.matches("svn: (E[0-9]{6}: )?URL 'file://.*/fake' doesn't exist\n?", message.trim());
            assertTrue(message, assertBoolean);
        }
    }

    /**
     * Test for {@link SubversionProjectImporter#importSources(org.eclipse.che.api.project.server.FolderEntry, org.eclipse.che.api.core.model.project.SourceStorage, org.eclipse.che.api.core.util.LineConsumerFactory)}
     * with a valid url.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testValidImportSources() throws Exception {
        final String projectName = NameGenerator.generate("project-", 3);
        final VirtualFile virtualFile = root.createFolder(projectName);
        FolderEntry projectFolder = new FolderEntry(virtualFile);
        String repoUrl = Paths.get(repoRoot.getAbsolutePath()).toUri().toString();
        when(sourceStorage.getLocation()).thenReturn(repoUrl);
        projectImporter.importSources(projectFolder, sourceStorage, new TestUtils.SystemOutLineConsumerFactory());

        assertTrue(projectFolder.getChild(".svn").isFolder());
        assertTrue(projectFolder.getChild("trunk").isFolder());
        assertTrue(projectFolder.getChildFolder("trunk").getChild("A").isFolder());
        assertTrue(projectFolder.getChildFolder("trunk").getChildFolder("A").getChild("mu").isFile());
    }
}
