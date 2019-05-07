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

import static org.eclipse.che.selenium.core.CheSeleniumSuiteModule.AUXILIARY;
import static org.eclipse.che.selenium.core.TestGroup.GITHUB;
import static org.eclipse.che.selenium.core.TestGroup.OPENSHIFT;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree;
import org.eclipse.che.selenium.pageobject.theia.TheiaProposalForm;
import org.eclipse.che.selenium.pageobject.theia.TheiaTerminal;
import org.openqa.selenium.Keys;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {GITHUB, OPENSHIFT})
public class DirectUrlFactoryWithSpecificBranchTest {
  private static final String SECOND_BRANCH_NAME = "contrib";

  @Inject
  @Named(AUXILIARY)
  private TestGitHubRepository testAuxiliaryRepo;

  @Inject private TestFactoryInitializer testFactoryInitializer;
  @Inject private TheiaIde theiaIde;
  @Inject private TheiaProjectTree theiaProjectTree;
  @Inject private TheiaTerminal theiaTerminal;
  @Inject private TheiaProposalForm theiaProposalForm;

  private TestFactory testFactoryWithSpecificBranch;

  @BeforeClass
  public void setUp() throws Exception {
    // preconditions - add the project to the test repository
    Path entryPath = Paths.get(getClass().getResource("/projects/java-multimodule").getPath());
    testAuxiliaryRepo.addContent(entryPath);
    String repositoryUrl = testAuxiliaryRepo.getHtmlUrl();

    // create another branch in the test repo
    testAuxiliaryRepo.createBranch(SECOND_BRANCH_NAME);

    testFactoryWithSpecificBranch =
        testFactoryInitializer.fromUrl(repositoryUrl + "/tree/" + SECOND_BRANCH_NAME);
  }

  @AfterClass
  public void deleteTestBranch() throws Exception {
    testFactoryWithSpecificBranch.delete();
  }

  @Test
  public void factoryWithDirectUrlWithSpecificBranch() {
    String repositoryName = testAuxiliaryRepo.getName();
    final String wsTheiaIdeTerminalTitle = "theia-ide terminal 0";
    List<String> expectedItemsAfterCloning = Arrays.asList("my-lib", "my-webapp", "src", "pom.xml");

    testFactoryWithSpecificBranch.authenticateAndOpen();

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
    theiaProjectTree.openItem(repositoryName);
    theiaProjectTree.openItem(repositoryName + "/my-lib");
    theiaProjectTree.waitItem(repositoryName + "/my-lib/src");

    expectedItemsAfterCloning.forEach(
        name -> {
          theiaProjectTree.isItemVisible(repositoryName + "/" + name);
        });

    // check specific branch
    openTerminalByProposal("theia-ide");
    theiaTerminal.waitTab(wsTheiaIdeTerminalTitle);
    theiaTerminal.clickOnTab(wsTheiaIdeTerminalTitle);
    theiaTerminal.performCommand("cd " + repositoryName);
    theiaTerminal.performCommand("git status");
    theiaTerminal.waitTerminalOutput("On branch " + SECOND_BRANCH_NAME, 0);
  }

  private void openTerminalByProposal(String proposalText) {
    theiaIde.pressKeyCombination(Keys.LEFT_CONTROL, "`");
    theiaProposalForm.waitProposal(proposalText);
    theiaProposalForm.clickOnProposal(proposalText);
    theiaProposalForm.waitFormDisappearance();
  }
}
