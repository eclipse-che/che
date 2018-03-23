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
package org.eclipse.che.selenium.editor.autocomplete;

import com.google.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
public class AutocompleteProposalJavaDocTest {
  private static final String PROJECT = "multi-module-java-with-ext-libs";

  private static final String APP_CLASS_NAME = "App";
  private static final String PATH_FOR_EXPAND_APP_CLASS =
      PROJECT + "/app/src/main/java/multimodule";
  private static final String BOOK_IMPL_CLASS_NAME = "BookImpl";
  private static final Logger LOG = LoggerFactory.getLogger(AutocompleteProposalJavaDocTest.class);
  private static final String PATH_FOR_EXPAND_IMPL_CLASS = "model/src/main/java/multimodule.model";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setup() throws Exception {
    URL resource = getClass().getResource("/projects/multi-module-java-with-ext-libs");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT,
        ProjectTemplates.CONSOLE_JAVA_SIMPLE);
    // open IDE
    ide.open(workspace);
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT);
    projectExplorer.waitAndSelectItem(PROJECT);
    notificationsPopupPanel.waitProgressPopupPanelClose();

    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        PATH_FOR_EXPAND_APP_CLASS, APP_CLASS_NAME + ".java");

    // close project tree
    projectExplorer.openItemByPath(PROJECT);

    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        PROJECT + "/model/src/main/java/multimodule.model", BOOK_IMPL_CLASS_NAME + ".java");
  }

  @BeforeMethod
  private void openMainClass() {
    editor.selectTabByName(APP_CLASS_NAME);
  }

  @Test
  public void shouldDisplayJavaDocOfClassMethod() throws Exception {
    // when
    editor.waitActive();
    loader.waitOnClosed();
    editor.goToCursorPositionVisible(30, 30);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectAutocompleteProposal("concat(String part1, String part2, char divider) : String");

    // then
    editor.waitContextMenuJavaDocText(
        ".*<p><b>Deprecated.</b> <i> As of version 1.0, use "
            + "<code><a href='.*/javadoc/get\\?.*projectpath=/multi-module-java-with-ext-libs/app&handle=%E2%98%82%3Dmulti-module-java-with-ext-libs.*org.apache.commons.lang.StringUtils%E2%98%82join%E2%98%82Object\\+%5B%5D%E2%98%82char'>org.apache.commons.lang.StringUtils.join\\(Object \\[\\], char\\)</a></code>"
            + "</i><p>Returns concatination of two strings into one divided by special symbol."
            + "<dl><dt>Parameters:</dt>"
            + "<dd><b>part1</b> part 1 to concat.</dd>"
            + "<dd><b>part2</b> part 2 to concat.</dd>"
            + "<dd><b>divider</b> divider of part1 and part2.</dd>"
            + "<dt>Returns:</dt><dd> concatination of two strings into one.</dd><dt>Throws:</dt>"
            + "<dd><a href='.*/javadoc/get\\?.*projectpath=/multi-module-java-with-ext-libs/app&handle=%E2%98%82%3Dmulti-module-java-with-ext-libs%5C%2Fapp%2Fsrc%5C%2Fmain%5C%2Fjava%3Cmultimodule%7BApp.java%E2%98%83App%7Econcat%7EQString%3B%7EQString%3B%7EC%E2%98%82NullPointerException'>NullPointerException</a>.*if one of the part has null value.</dd></dl>.*");
  }

  @Test
  public void shouldWorkAroundAbsentJavaDocOfConstructor() throws IOException {
    // when
    editor.waitActive();
    loader.waitOnClosed();
    editor.goToCursorPositionVisible(19, 1);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectAutocompleteProposal("App()");

    // then
    editor.waitContextMenuJavaDocText(".*No documentation found.*");
  }

  @Test
  public void shouldDisplayAnotherModuleClassJavaDoc() throws IOException {
    // when
    editor.waitActive();
    loader.waitOnClosed();
    editor.goToCursorPositionVisible(24, 20);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectAutocompleteProposal("isEquals(Object o) : boolean");

    // then
    editor.waitContextMenuJavaDocText(
        ".*Returns <code>true</code> if the argument is equal to instance. otherwise <code>false</code>"
            + "<dl><dt>Parameters:</dt>"
            + "<dd><b>o</b> an object.</dd>"
            + "<dt>Returns:</dt>"
            + "<dd> Returns <code>true</code> if the argument is equal to instance. otherwise <code>false</code></dd>"
            + "<dt>Since:</dt>"
            + "<dd> 1.0</dd>"
            + "<dt>See Also:</dt>.*"
            + "<dd><a href='.*/javadoc/get\\?.*projectpath=/multi-module-java-with-ext-libs/model&handle=%E2%98%82%3Dmulti-module-java-with-ext-libs%5C%2Fmodel%2Fsrc%5C%2Fmain%5C%2Fjava%3Cmultimodule.model%7BBook.java%E2%98%83Book%7EisEquals%7EQObject%3B%E2%98%82java.lang.Object%E2%98%82equals%E2%98%82Object'>java.lang.Object.equals\\(Object\\)</a></dd></dl>.*");
  }

  @Test
  public void shouldReflectChangesInJavaDoc() throws IOException {
    // when
    editor.waitActive();
    loader.waitOnClosed();
    editor.selectTabByName(BOOK_IMPL_CLASS_NAME);
    editor.goToCursorPositionVisible(14, 4);
    editor.typeTextIntoEditor("UPDATE. ");

    editor.selectTabByName(APP_CLASS_NAME);
    editor.waitActive();
    editor.goToCursorPositionVisible(21, 12);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectAutocompleteProposal("BookImpl");

    // then
    editor.waitContextMenuJavaDocText(".*UPDATE. Implementation of Book interface..*");
  }

  @Test
  public void shouldDisplayJavaDocOfJreClass() throws IOException {
    // when
    editor.waitActive();
    loader.waitOnClosed();
    editor.goToCursorPositionVisible(24, 20);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectAutocompleteProposal("hashCode() : int");

    // then
    editor.waitContextMenuJavaDocText(
        ".*Returns a hash code value for the object. "
            + "This method is supported for the benefit of hash tables such as those provided by "
            + "<code><a href='.*/javadoc/get\\?.*projectpath=/multi-module-java-with-ext-libs/app&handle=%E2%98%82%3Dmulti-module-java-with-ext-libs.*%3Cjava.lang%28Object.class%E2%98%83Object%7EhashCode%E2%98%82java.util.HashMap'>java.util.HashMap</a></code>.*");
  }

  @Test
  public void shouldWorkAroundAbsentSourcesOfExternalLib() throws IOException {
    // when
    editor.waitActive();
    loader.waitOnClosed();
    editor.goToCursorPositionVisible(30, 23);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectAutocompleteProposal("info(String arg0) : void");

    // then
    editor.waitContextMenuJavaDocText(".*No documentation found.*");

    // when
    editor.closeAutocomplete();
    editor.typeTextIntoEditor(Keys.F4.toString());
    editor.waitActiveTabFileName(
        "Logger"); // there should be class "Logger" opened in decompiled view with "Download
    // sources" link at the top.
    editor.clickOnDownloadSourcesLink(); // there should be "Download sources" link displayed in at
    // the top of editor. Download they.

    editor.selectTabByName(APP_CLASS_NAME);
    loader.waitOnClosed();
    editor.goToCursorPositionVisible(30, 23);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectAutocompleteProposal("info(String msg) : void");

    // then
    editor.waitContextMenuJavaDocText(
        ".*Log a message at the .* level."
            + "<dl><dt>Parameters:</dt>"
            + "<dd><b>msg</b>"
            + "  the message string to be logged</dd></dl>.*");
  }
}
