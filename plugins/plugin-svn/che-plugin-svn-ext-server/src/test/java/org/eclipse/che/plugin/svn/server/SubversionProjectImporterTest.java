/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.svn.server;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubversionProjectImporterTest {

  //  @Mock private ProfileDao userProfileDao;
  //  @Mock private RepositoryUrlProvider repositoryUrlProvider;
  //  @Mock private SourceStorage sourceStorage;
  //  @Mock private SshKeyProvider sshKeyProvider;
  //
  //  private File repoRoot;
  //  private SubversionProjectImporter projectImporter;
  //  private VirtualFile root;
  //
  //  @Before
  //  public void setUp() throws Exception {
  //    // Bind components
  //    Injector injector =
  //        Guice.createInjector(
  //            new AbstractModule() {
  //              @Override
  //              protected void configure() {
  //                Multibinder.newSetBinder(
  //                        binder(), org.eclipse.che.api.project.server.api.ProjectImporter.class)
  //                    .addBinding()
  //                    .to(SubversionProjectImporter.class);
  //                Multibinder.newSetBinder(binder(), ProjectTypeDef.class)
  //                    .addBinding()
  //                    .to(SubversionProjectType.class);
  //                Multibinder.newSetBinder(binder(), ValueProviderFactory.class)
  //                    .addBinding()
  //                    .to(SubversionValueProviderFactory.class);
  //
  //                bind(SshKeyProvider.class).toInstance(sshKeyProvider);
  //                bind(ProfileDao.class).toInstance(userProfileDao);
  //                bind(RepositoryUrlProvider.class).toInstance(repositoryUrlProvider);
  //              }
  //            });
  //
  //    // Init virtual file system
  //    VirtualFileSystem virtualFileSystem = TestUtils.createVirtualFileSystem();
  //    root = virtualFileSystem.getRoot();
  //
  //    // Create the test user
  //    TestUtils.createTestUser(userProfileDao);
  //
  //    // Create the Subversion repository
  //    repoRoot = TestUtils.createGreekTreeRepository();
  //
  //    projectImporter = injector.getInstance(SubversionProjectImporter.class);
  //  }
  //
  //  /**
  //   * Test for {@link SubversionProjectImporter#getSourceCategory()}.
  //   *
  //   * @throws Exception if anything goes wrong
  //   */
  //  @Test
  //  public void testGetCategory() throws Exception {
  //    //    assertEquals(projectImporter.getSourceCategory(), ProjectImporter.ImporterCategory.SOURCE_CONTROL);
  //  }
  //
  //  /**
  //   * Test for {@link SubversionProjectImporter#getDescription()}.
  //   *
  //   * @throws Exception if anything goes wrong
  //   */
  //  @Test
  //  public void testGetDescription() throws Exception {
  //    assertEquals(
  //        projectImporter.getDescription(), "Import project from Subversion repository URL.");
  //  }
  //
  //  /**
  //   * Test for {@link SubversionProjectImporter#getId()}
  //   *
  //   * @throws Exception if anything goes wrong
  //   */
  //  @Test
  //  public void testGetId() throws Exception {
  //    assertEquals(projectImporter.getId(), "subversion");
  //  }
  //
  //  /**
  //   * Test for {@link SubversionProjectImporter#isInternal()}.
  //   *
  //   * @throws Exception if anything goes wrong
  //   */
  //  @Test
  //  public void testIsInternal() throws Exception {
  //    assertEquals(projectImporter.isInternal(), false);
  //  }
  //
  //  /**
  //   * Test for {@link SubversionProjectImporter#doImport(SourceStorage, String, Supplier)} invalid
  //   * url.
  //   *
  //   * @throws Exception if anything goes wrong
  //   */
  //  @Test
  //  public void testInvalidImportSources() throws Exception {
  //    final String projectName = NameGenerator.generate("project-", 3);
  //    final VirtualFile virtualFile =
  //        root.createFolder(projectName); //root.getChild(Path.of(projectName));
  //    FolderEntry projectFolder = new FolderEntry(virtualFile);
  //    // TODO
  //    //    try {
  //    //      String fakeUrl = Paths.get(repoRoot.getAbsolutePath()).toUri() + "fake";
  //    //      when(sourceStorage.getLocation()).thenReturn(fakeUrl);
  //    //      projectImporter.importSources(
  //    //          projectFolder, sourceStorage, new TestUtils.SystemOutLineConsumerFactory());
  //
  //    //      fail("The code above should had failed");
  //    //    } catch (SubversionException e) {
  //    //      final String message = e.getMessage();
  //    //
  //    //      boolean assertBoolean =
  //    //          Pattern.matches(
  //    //              "svn: (E[0-9]{6}: )?URL 'file://.*/fake' doesn't exist\n?", message.trim());
  //    //      assertTrue(message, assertBoolean);
  //    //    }
  //  }
  //
  //  /**
  //   * Test for {@link SubversionProjectImporter#doImport(SourceStorage, String, Supplier)} with a
  //   * valid url.
  //   *
  //   * @throws Exception if anything goes wrong
  //   */
  //  @Test
  //  public void testValidImportSources() throws Exception {
  //    final String projectName = NameGenerator.generate("project-", 3);
  //    final VirtualFile virtualFile = root.createFolder(projectName);
  //    FolderEntry projectFolder = new FolderEntry(virtualFile);
  //    String repoUrl = Paths.get(repoRoot.getAbsolutePath()).toUri().toString();
  //    when(sourceStorage.getLocation()).thenReturn(repoUrl);
  //    // TODO
  //    //    projectImporter.importSources(
  //    //        projectFolder, sourceStorage, new TestUtils.SystemOutLineConsumerFactory());
  //    //
  //    //    assertTrue(projectFolder.getChild(".svn").isFolder());
  //    //    assertTrue(projectFolder.getChild("trunk").isFolder());
  //    //    assertTrue(projectFolder.getChildFolder("trunk").getChild("A").isFolder());
  //    //    assertTrue(projectFolder.getChildFolder("trunk").getChildFolder("A").getChild("mu").isFile());
  //  }
}
