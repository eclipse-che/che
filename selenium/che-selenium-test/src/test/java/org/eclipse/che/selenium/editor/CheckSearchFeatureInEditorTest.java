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

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.SearchReplacePanel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CheckSearchFeatureInEditorTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(CheckSearchFeatureInEditorTest.class.getSimpleName(), 4);
  private static final String FIND_TEXT = "Num";
  private static final String FIND_TEXT_REGULAR_EXP = "Num.*";
  private static final String FIRST_CURSOR_POSITION = "22:42";
  private static final String SECOND_CURSOR_POSITION = "26:19";
  private static final String THIRD_CURSOR_POSITION = "38:28";
  private static final String FOURTH_CURSOR_POSITION = "30:60";
  private static final String FIFTH_CURSOR_POSITION = "30:61";
  private static final String SIXTH_CURSOR_POSITION = "22:88";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private SearchReplacePanel searchReplacePanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test(priority = 0)
  public void checkNextPrevious() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    loader.waitOnClosed();
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    editor.waitActive();
    searchReplacePanel.openSearchReplacePanel();
    searchReplacePanel.enterTextInFindInput(FIND_TEXT);
    WaitUtils.sleepQuietly(2);
    editor.waitSpecifiedValueForLineAndChar(FIRST_CURSOR_POSITION);
    searchReplacePanel.clickOnNextBtn();
    editor.waitSpecifiedValueForLineAndChar(SECOND_CURSOR_POSITION);
    searchReplacePanel.clickOnPreviousBtn();
    editor.waitSpecifiedValueForLineAndChar(FIRST_CURSOR_POSITION);
  }

  @Test(priority = 1)
  public void checkToggleWholeWord() {
    searchReplacePanel.clickOnToggleWholeBtn();
    searchReplacePanel.clickOnNextBtn();
    editor.waitSpecifiedValueForLineAndChar(THIRD_CURSOR_POSITION);
    searchReplacePanel.clickOnPreviousBtn();
    editor.waitSpecifiedValueForLineAndChar(THIRD_CURSOR_POSITION);
  }

  @Test(priority = 2)
  public void checkToggleCaseInsensitive() {
    searchReplacePanel.clickOnToggleWholeBtn();
    searchReplacePanel.clickOnToggleCaseBtn();
    searchReplacePanel.clickOnPreviousBtn();
    editor.waitSpecifiedValueForLineAndChar(FOURTH_CURSOR_POSITION);
    searchReplacePanel.clickOnNextBtn();
    editor.waitSpecifiedValueForLineAndChar(FIRST_CURSOR_POSITION);
  }

  @Test(priority = 3)
  public void checkToggleRegularExpression() {
    searchReplacePanel.enterTextInFindInput(FIND_TEXT_REGULAR_EXP);
    searchReplacePanel.clickOnToggleRegularExpressionBtn();
    searchReplacePanel.clickOnPreviousBtn();
    editor.waitSpecifiedValueForLineAndChar(FIFTH_CURSOR_POSITION);
    searchReplacePanel.clickOnNextBtn();
    editor.waitSpecifiedValueForLineAndChar(SIXTH_CURSOR_POSITION);
  }
}
