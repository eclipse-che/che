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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

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
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.PopupDialogsBrowser;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckRestoringOfSplitEditor {
  private String nameJavaClass = "AppController.java";
  private String nameReadmeFile = "README.md";
  private String namePomFile = "pom.xml";
  private String nameOfTabPomFile = "qa-spring-sample";
  private final String PROJECT_NAME =
      NameGenerator.generate(CheckRestoringOfSplitEditor.class.getSimpleName(), 4);
  private final String PATH_TO_JAVA_FILE =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/" + nameJavaClass;
  private Pair<Integer, Integer> cursorPositionForJavaFile = new Pair<>(12, 1);
  private Pair<Integer, Integer> cursorPositionForReadMeFile = new Pair<>(1, 10);
  private Pair<Integer, Integer> cursorPositionForPomFile = new Pair<>(31, 1);

  private String expectedTextFromFile = "";
  private String splitter = "----split_line---";
  private List<String> expectedText;

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private PopupDialogsBrowser popupDialogsBrowser;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resources =
        CheckRestoringOfSplitEditor.class.getResource("split-editor-restore-exp-text.txt");
    expectedText = Files.readAllLines(Paths.get(resources.toURI()), Charset.forName("UTF8"));
    expectedTextFromFile = Joiner.on("\n").join(expectedText);

    expectedText = Arrays.asList(expectedTextFromFile.split(splitter));
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_JAVA_MULTIMODULE);
    ide.open(workspace);
  }

  @Test
  public void checkRestoringStateSplittedEditor() throws IOException {
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_TO_JAVA_FILE);
    splitEditorAndOpenFiles();
    setPositionsForSplittedEditor();

    editor.waitActiveEditor();
    if (popupDialogsBrowser.isAlertPresent()) {
      popupDialogsBrowser.acceptAlert();
    }
    ide.driver().navigate().refresh();
    checkSplitdEditorAfterRefreshing(
        1, nameJavaClass.split("\\.")[0], expectedText.get(0), cursorPositionForJavaFile);
    checkSplitdEditorAfterRefreshing(
        2, nameReadmeFile, expectedText.get(1).trim(), cursorPositionForReadMeFile);
    checkSplitdEditorAfterRefreshing(
        3, nameOfTabPomFile, expectedText.get(2).trim(), cursorPositionForPomFile);
  }

  private void checkSplitdEditorAfterRefreshing(
      int numOfEditor,
      String nameOfEditorTab,
      String expectedTextAfterRefresh,
      Pair<Integer, Integer> pair)
      throws IOException {

    editor.waitActiveEditor();
    editor.selectTabByName(nameOfEditorTab);
    editor.waitSpecifiedValueForLineAndChar(pair.first, pair.second);
    editor.waitTextInDefinedSplitEditor(
        numOfEditor, REDRAW_UI_ELEMENTS_TIMEOUT_SEC, expectedTextAfterRefresh);
  }

  private void splitEditorAndOpenFiles() {
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_TO_JAVA_FILE);
    loader.waitOnClosed();
    editor.waitActiveEditor();
    editor.openContextMenuForTabByName(nameJavaClass.split("\\.")[0]);
    editor.runActionForTabFromContextMenu(CodenvyEditor.TabAction.SPIT_HORISONTALLY);
    editor.selectTabByIndexEditorWindowAndOpenMenu(0, nameJavaClass.split("\\.")[0]);
    editor.runActionForTabFromContextMenu(CodenvyEditor.TabAction.SPLIT_VERTICALLY);
    editor.selectTabByIndexEditorWindow(1, nameJavaClass.split("\\.")[0]);
    projectExplorer.openItemByPath(PROJECT_NAME + "/" + nameReadmeFile);
    editor.selectTabByIndexEditorWindow(2, nameJavaClass.split("\\.")[0]);
    projectExplorer.openItemByPath(PROJECT_NAME + "/" + namePomFile);
  }

  private void setPositionsForSplittedEditor() {
    editor.selectTabByIndexEditorWindow(0, nameJavaClass.split("\\.")[0]);
    editor.setCursorToDefinedLineAndChar(
        cursorPositionForJavaFile.first, cursorPositionForJavaFile.second);
    editor.selectTabByName(nameReadmeFile);
    editor.setCursorToDefinedLineAndChar(
        cursorPositionForReadMeFile.first, cursorPositionForReadMeFile.second);
    editor.selectTabByName(nameOfTabPomFile);
    editor.setCursorToDefinedLineAndChar(
        cursorPositionForPomFile.first, cursorPositionForPomFile.second);
  }
}
