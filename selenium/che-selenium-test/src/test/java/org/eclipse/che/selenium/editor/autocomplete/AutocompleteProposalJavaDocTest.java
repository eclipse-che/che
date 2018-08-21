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
package org.eclipse.che.selenium.editor.autocomplete;

import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
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

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  // no links are present in completion item javadoc due to
  // https://github.com/eclipse/eclipse.jdt.ls/issues/731
  // also, links would be displayed, but not handled currently.

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
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT);
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
    editor.goToCursorPositionVisible(31, 30);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectAutocompleteProposal(
        "concat(String part1, String part2, char divider) : String App");

    // then
    editor.waitProposalDocumentationHTML(
        "<p><strong>Deprecated</strong>  <em>As of version 1.0, use <a href=\"jdt://contents/commons-lang-2.6.jar/org.apache.commons.lang/StringUtils.class?=app/%5C/home%5C/user%5C/.m2%5C/repository%5C/commons-lang%5C/commons-lang%5C/2.6%5C/commons-lang-2.6.jar%3Corg.apache.commons.lang%28StringUtils.class#3169\">org.apache.commons.lang.StringUtils.join(Object [], char)</a></em></p>\n"
            + "<p>Returns concatination of two strings into one divided by special symbol.</p>\n"
            + "<ul>\n"
            + "<li><p><strong>Parameters:</strong></p>\n"
            + "<ul>\n"
            + "<li><p><strong>part1</strong> part 1 to concat.</p>\n"
            + "</li>\n"
            + "<li><p><strong>part2</strong> part 2 to concat.</p>\n"
            + "</li>\n"
            + "<li><p><strong>divider</strong> divider of part1 and part2.</p>\n"
            + "</li>\n"
            + "</ul>\n"
            + "</li>\n"
            + "<li><p><strong>Returns:</strong></p>\n"
            + "<ul>\n"
            + "<li>concatination of two strings into one.</li>\n"
            + "</ul>\n"
            + "</li>\n"
            + "<li><p><strong>Throws:</strong></p>\n"
            + "<ul>\n"
            + "<li><a href=\"jdt://contents/rt.jar/java.lang/NullPointerException.class?=app/%5C/usr%5C/lib%5C/jvm%5C/java-8-openjdk-amd64%5C/jre%5C/lib%5C/rt.jar%3Cjava.lang%28NullPointerException.class#53\">NullPointerException</a> - if one of the part has null value.</li>\n"
            + "</ul>\n"
            + "</li>\n"
            + "</ul>\n");
  }

  @Test
  public void shouldWorkAroundAbsentJavaDocOfConstructor() throws IOException {
    // when
    editor.waitActive();
    loader.waitOnClosed();
    editor.goToCursorPositionVisible(32, 14);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectAutocompleteProposal("App() multimodule.App");

    // then
    editor.waitProposalDocumentationHTML("<p>No documentation found.</p>\n");
  }

  @Test
  public void shouldDisplayAnotherModuleClassJavaDoc() throws IOException {
    // when
    editor.waitActive();
    loader.waitOnClosed();
    editor.goToCursorPositionVisible(25, 20);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectAutocompleteProposal("isEquals(Object o) : boolean Book");

    // then
    editor.waitProposalDocumentationHTML(
        "<p>Returns <code>true</code> if the argument is equal to instance. otherwise <code>false</code></p>\n"
            + "<ul>\n"
            + "<li><p><strong>Parameters:</strong></p>\n"
            + "<ul>\n"
            + "<li><strong>o</strong> an object.</li>\n"
            + "</ul>\n"
            + "</li>\n"
            + "<li><p><strong>Returns:</strong></p>\n"
            + "<ul>\n"
            + "<li>Returns <code>true</code> if the argument is equal to instance. otherwise <code>false</code></li>\n"
            + "</ul>\n"
            + "</li>\n"
            + "<li><p><strong>Since:</strong></p>\n"
            + "<ul>\n"
            + "<li>1.0</li>\n"
            + "</ul>\n"
            + "</li>\n"
            + "<li><p><strong>See Also:</strong></p>\n"
            + "<ul>\n"
            + "<li><a href=\"jdt://contents/rt.jar/java.lang/Object.class?=app/%5C/usr%5C/lib%5C/jvm%5C/java-8-openjdk-amd64%5C/jre%5C/lib%5C/rt.jar%3Cjava.lang%28Object.class#148\">java.lang.Object.equals(Object)</a></li>\n"
            + "</ul>\n"
            + "</li>\n"
            + "</ul>\n");
  }

  @Test
  public void shouldReflectChangesInJavaDoc() throws IOException {
    // when
    editor.waitActive();
    loader.waitOnClosed();
    editor.selectTabByName(BOOK_IMPL_CLASS_NAME);
    editor.goToCursorPositionVisible(15, 4);
    editor.typeTextIntoEditor("UPDATE. ");

    editor.selectTabByName(APP_CLASS_NAME);
    editor.waitActive();
    editor.goToCursorPositionVisible(22, 12);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectAutocompleteProposal("BookImpl - multimodule.model");

    // then
    editor.waitProposalDocumentationHTML("UPDATE. Implementation of Book interface.");
  }

  @Test
  public void shouldDisplayJavaDocOfJreClass() throws IOException {
    // when
    editor.waitActive();
    loader.waitOnClosed();
    editor.goToCursorPositionVisible(25, 20);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectAutocompleteProposal("hashCode() : int Object");

    // then
    editor.waitProposalDocumentationHTML(
        "Returns a hash code value for the object. "
            + "This method is supported for the benefit of hash tables such as those provided by");
  }

  @Test
  public void shouldWorkAroundAbsentSourcesOfExternalLib() throws IOException {

    // This test fails because jdt.ls does download source for the class being used here. Need to
    // find or construct an artifact that has no source.
    // when
    editor.waitActive();
    loader.waitOnClosed();
    editor.goToCursorPositionVisible(31, 23);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectAutocompleteProposal("info(String msg) : void Logger");

    // then
    editor.waitProposalDocumentationHTML(".*No documentation found.*");

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
    editor.goToCursorPositionVisible(31, 23);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectAutocompleteProposal("info(String msg) : void");

    // then
    assertEquals(
        editor.getProposalDocumentationHTML(),
        ".*Log a message at the .* level."
            + "<dl><dt>Parameters:</dt>"
            + "<dd><b>msg</b>"
            + "  the message string to be logged</dd></dl>.*");
  }
}
