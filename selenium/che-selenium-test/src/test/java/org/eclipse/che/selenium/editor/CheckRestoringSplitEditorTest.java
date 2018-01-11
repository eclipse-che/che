/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.editor;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_JAVA_MULTIMODULE;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.TabAction.SPIT_HORISONTALLY;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.TabAction.SPLIT_VERTICALLY;
import static org.testng.Assert.fail;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.PopupDialogsBrowser;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckRestoringSplitEditorTest {
  private String javaClassName = "AppController.java";
  private String readmeFileName = "README.md";
  private String pomFileTab = "qa-spring-sample";
  private String javaClassTab = "AppController";
  private final String PROJECT_NAME = NameGenerator.generate("project", 4);;
  private final String PATH_TO_JAVA_FILE =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/" + javaClassName;
  private Pair<Integer, Integer> cursorPositionForJavaFile = new Pair<>(12, 1);
  private Pair<Integer, Integer> cursorPositionForReadMeFile = new Pair<>(1, 10);
  private Pair<Integer, Integer> cursorPositionForPomFile = new Pair<>(31, 1);
  private List<String> expectedTextFromEditor;

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private PopupDialogsBrowser popupDialogsBrowser;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;

  @BeforeClass
  public void prepare() throws Exception {
    String splitter = "----split_line---";
    URL resources =
        CheckRestoringSplitEditorTest.class.getResource("split-editor-restore-exp-text.txt");
    expectedTextFromEditor =
        Files.readAllLines(Paths.get(resources.toURI()), Charset.forName("UTF8"));
    String expectedTextFromFile = Joiner.on("\n").join(expectedTextFromEditor);
    expectedTextFromEditor = Arrays.asList(expectedTextFromFile.split(splitter));
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, MAVEN_JAVA_MULTIMODULE);
    ide.open(workspace);
  }

  @Test
  public void checkRestoringStateSplittedEditor() throws IOException {
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    splitEditorAndOpenFiles();
    setPositionsForSplittedEditor();
    editor.waitActive();
    if (popupDialogsBrowser.isAlertPresent()) {
      popupDialogsBrowser.acceptAlert();
    }

    seleniumWebDriver.navigate().refresh();
    projectExplorer.waitItem(PROJECT_NAME);
    try {
      projectExplorer.waitItemInVisibleArea(javaClassName);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7551", ex);
    }

    notificationsPopupPanel.waitPopUpPanelsIsClosed();
    checkSplitdEditorAfterRefreshing(
        1, javaClassTab, expectedTextFromEditor.get(0), cursorPositionForJavaFile);
    checkSplitdEditorAfterRefreshing(
        2, readmeFileName, expectedTextFromEditor.get(1).trim(), cursorPositionForReadMeFile);
    checkSplitdEditorAfterRefreshing(
        3, pomFileTab, expectedTextFromEditor.get(2).trim(), cursorPositionForPomFile);
  }

  private void checkSplitdEditorAfterRefreshing(
      int numOfEditor,
      String nameOfEditorTab,
      String expectedTextAfterRefresh,
      Pair<Integer, Integer> pair)
      throws IOException {

    editor.waitActive();
    editor.selectTabByName(nameOfEditorTab);
    editor.waitCursorPosition(pair.first, pair.second);
    editor.waitTextInDefinedSplitEditor(
        numOfEditor, LOAD_PAGE_TIMEOUT_SEC, expectedTextAfterRefresh);
  }

  private void splitEditorAndOpenFiles() {
    String namePomFile = "pom.xml";

    projectExplorer.openItemByPath(PATH_TO_JAVA_FILE);
    loader.waitOnClosed();
    editor.waitActive();
    editor.openContextMenuForTabByName(javaClassTab);
    editor.runActionForTabFromContextMenu(SPIT_HORISONTALLY);
    editor.selectTabByIndexEditorWindowAndOpenMenu(0, javaClassTab);
    editor.runActionForTabFromContextMenu(SPLIT_VERTICALLY);
    loader.waitOnClosed();
    editor.selectTabByIndexEditorWindow(1, javaClassTab);
    projectExplorer.openItemByPath(PROJECT_NAME + "/" + readmeFileName);
    editor.selectTabByIndexEditorWindow(2, javaClassTab);
    projectExplorer.openItemByPath(PROJECT_NAME + "/" + namePomFile);
  }

  private void setPositionsForSplittedEditor() {
    editor.selectTabByIndexEditorWindow(0, javaClassTab);
    editor.goToCursorPositionVisible(
        cursorPositionForJavaFile.first, cursorPositionForJavaFile.second);
    editor.selectTabByName(readmeFileName);
    editor.goToPosition(cursorPositionForReadMeFile.first, cursorPositionForReadMeFile.second);
    editor.selectTabByName(pomFileTab);
    editor.goToPosition(cursorPositionForPomFile.first, cursorPositionForPomFile.second);
  }
}
