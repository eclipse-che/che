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
package org.eclipse.che.selenium.editor;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.SearchReplacePanel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CheckReplaceFeatureInEditorTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(CheckReplaceFeatureInEditorTest.class.getSimpleName(), 4);
  private static final String FIND_TEXT = "codenvy";
  private static final String REPLACE_TEXT = "che";

  private String expectedReplace = "";
  private String expectedReplaceAll = "";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private SearchReplacePanel searchReplacePanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    URL resources = CheckReplaceFeatureInEditorTest.class.getResource("replace");
    List<String> expectedReplaseTextList =
        Files.readAllLines(Paths.get(resources.toURI()), Charset.forName("UTF-8"));
    expectedReplace = Joiner.on("\n").join(expectedReplaseTextList);

    resources = CheckReplaceFeatureInEditorTest.class.getResource("replace-all");
    List<String> expectedReplaseAllTextList =
        Files.readAllLines(Paths.get(resources.toURI()), Charset.forName("UTF-8"));
    expectedReplaceAll = Joiner.on("\n").join(expectedReplaseAllTextList);

    URL resource = getClass().getResource("/projects/defaultSpringProjectWithDifferentTypeOfFiles");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);

    ide.open(workspace);
  }

  @Test(priority = 0)
  public void checkReplace() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitPopUpPanelsIsClosed();
    consoles.closeProcessesArea();
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    projectExplorer.openItemByVisibleNameInExplorer("README.md");
    loader.waitOnClosed();
    searchReplacePanel.openSearchReplacePanel();
    searchReplacePanel.enterTextInFindInput(FIND_TEXT);
    searchReplacePanel.enterTextInReplaceInput(REPLACE_TEXT);
    searchReplacePanel.clickOnReplaceBtn();
    searchReplacePanel.closeSearchReplacePanel();
    editor.closeAllTabs();
    projectExplorer.openItemByVisibleNameInExplorer("README.md");
    loader.waitOnClosed();
    editor.waitActive();
    editor.waitTextIntoEditor(expectedReplace);
  }

  @Test(priority = 1)
  public void checkReplaceAll() {
    searchReplacePanel.openSearchReplacePanel();
    searchReplacePanel.enterTextInFindInput(FIND_TEXT);
    searchReplacePanel.enterTextInReplaceInput(REPLACE_TEXT);
    searchReplacePanel.clickOnReplaceAllBtn();
    searchReplacePanel.closeSearchReplacePanel();
    editor.closeAllTabs();
    projectExplorer.openItemByVisibleNameInExplorer("README.md");
    loader.waitOnClosed();
    editor.waitActive();
    editor.waitTextIntoEditor(expectedReplaceAll);
  }
}
