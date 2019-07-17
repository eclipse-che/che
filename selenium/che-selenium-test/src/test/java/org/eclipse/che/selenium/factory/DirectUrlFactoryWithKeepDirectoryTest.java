/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.factory;

import static org.eclipse.che.selenium.core.TestGroup.GITHUB;
import static org.eclipse.che.selenium.core.TestGroup.OPENSHIFT;
import static org.eclipse.che.selenium.core.TestGroup.UNDER_REPAIR;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {GITHUB, OPENSHIFT, UNDER_REPAIR})
public class DirectUrlFactoryWithKeepDirectoryTest {
  private static final Logger LOG =
      LoggerFactory.getLogger(DirectUrlFactoryWithKeepDirectoryTest.class);

  @Inject private TestFactoryInitializer testFactoryInitializer;
  @Inject private TestGitHubRepository testRepo;
  @Inject private TheiaIde theiaIde;
  @Inject private TheiaProjectTree theiaProjectTree;

  private TestFactory testFactoryWithKeepDir;

  @BeforeClass
  public void setUp() throws Exception {
    // preconditions - add the project to the test repository
    Path entryPath = Paths.get(getClass().getResource("/projects/java-multimodule").getPath());
    testRepo.addContent(entryPath);
    String repositoryUrl = testRepo.getHtmlUrl();

    testFactoryWithKeepDir = testFactoryInitializer.fromUrl(repositoryUrl + "/tree/master/my-lib");
  }

  @AfterClass
  public void tearDown() throws Exception {
    try {
      testFactoryWithKeepDir.delete();
    } catch (Exception e) {
      LOG.warn("It was impossible to remove factory.", e);
    }
  }

  @Test
  public void factoryWithDirectUrlWithKeepDirectory() {
    String repositoryName = testRepo.getName();
    testFactoryWithKeepDir.authenticateAndOpen();

    theiaIde.switchToIdeFrame();
    theiaIde.waitTheiaIde();
    theiaIde.waitLoaderInvisibility();
    theiaIde.waitNotificationEqualsTo("Che Workspace: Finished cloning projects.");
    theiaIde.waitNotificationDisappearance(
        "Che Workspace: Finished cloning projects.", UPDATING_PROJECT_TIMEOUT_SEC);

    theiaProjectTree.waitFilesTab();
    theiaProjectTree.clickOnFilesTab();
    theiaProjectTree.waitProjectsRootItem();

    theiaProjectTree.waitItem(repositoryName);
    theiaProjectTree.openItem(repositoryName + "/my-lib");
    theiaProjectTree.waitItem(repositoryName + "/my-lib/src");

    Assert.assertTrue(theiaProjectTree.isItemVisible(repositoryName + "/my-lib/pom.xml"));

    try {
      Assert.assertFalse(theiaProjectTree.isItemVisible(repositoryName + "/my-webapp"));
    } catch (AssertionError ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/12311");
    }
  }
}
