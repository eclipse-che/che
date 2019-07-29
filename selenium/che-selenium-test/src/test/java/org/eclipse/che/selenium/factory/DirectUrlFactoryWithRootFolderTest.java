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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;

import com.google.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
@Test(groups = {GITHUB, OPENSHIFT})
public class DirectUrlFactoryWithRootFolderTest {
  private static final Logger LOG =
      LoggerFactory.getLogger(DirectUrlFactoryWithRootFolderTest.class);

  @Inject private TestFactoryInitializer testFactoryInitializer;
  @Inject private TestGitHubRepository testRepo;
  @Inject private TheiaIde theiaIde;
  @Inject private TheiaProjectTree theiaProjectTree;

  private TestFactory testFactoryWithRootFolder;

  @BeforeClass
  public void setUp() throws Exception {
    // preconditions - add the project to the test repository
    Path entryPath = Paths.get(getClass().getResource("/projects/quickstart").getPath());
    testRepo.addContent(entryPath);
    String repositoryUrl = testRepo.getHtmlUrl();

    testFactoryWithRootFolder = testFactoryInitializer.fromUrl(repositoryUrl);
  }

  @AfterClass
  public void tearDown() throws Exception {
    try {
      testFactoryWithRootFolder.delete();
    } catch (Exception e) {
      LOG.warn("It was impossible to remove factory.", e);
    }
  }

  @Test
  public void factoryWithDirectUrlWithRootFolder() {
    String repositoryName = testRepo.getName();
    List<String> expectedItemsAfterCloning =
        Arrays.asList(
            "CHANGELOG.md",
            "Dockerfile",
            "LICENSE.txt",
            "README.md",
            "favicon.ico",
            "index.html",
            "karma-test-shim.js",
            "karma.conf.js",
            "package.json",
            "protractor.config.js",
            "styles.css",
            "systemjs.config.js",
            "tsconfig.json",
            "tslint.json",
            "typings.json",
            "wallaby.js");

    testFactoryWithRootFolder.authenticateAndOpen();

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
    theiaProjectTree.expandItem(repositoryName);

    expectedItemsAfterCloning.forEach(
        name -> {
          theiaProjectTree.waitItem(repositoryName + "/" + name);
        });
  }
}
