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

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.TestGroup.UNDER_REPAIR;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.CUSTOM;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
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
  private static final String PATH_TO_SLF4J_ARTIFACTS = "/home/user/.m2/repository/org/slf4j";
  private static final String COMMAND_TO_REMOVE_SLF4J_ARTIFACTS = "removeSLF4J";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private CommandsPalette commandsPalette;
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
    testCommandServiceClient.createCommand(
        format("rm -r %s", PATH_TO_SLF4J_ARTIFACTS),
        COMMAND_TO_REMOVE_SLF4J_ARTIFACTS,
        CUSTOM,
        workspace.getId());
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

  @Test(groups = UNDER_REPAIR)
  public void shouldDisplayJavaDocOfClassMethod() throws Exception {
    // given
    final String expectedJavadocHtmlText =
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
            + "</ul>\n";

    // when
    editor.waitActive();
    loader.waitOnClosed();
    editor.goToCursorPositionVisible(31, 30);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectCompositeAutocompleteProposal(
        "concat(String part1, String part2, char divider) : String App");

    // then
    checkProposalDocumentationHTML(expectedJavadocHtmlText);
  }

  @Test
  public void shouldWorkAroundAbsentJavaDocOfConstructor() throws IOException {
    // when
    editor.waitActive();
    loader.waitOnClosed();
    editor.goToCursorPositionVisible(32, 14);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectCompositeAutocompleteProposal("App() multimodule.App");

    // then
    checkProposalDocumentationHTML("<p>No documentation found.</p>\n");
  }

  @Test(groups = UNDER_REPAIR)
  public void shouldDisplayAnotherModuleClassJavaDoc() throws IOException {
    // when
    editor.waitActive();
    loader.waitOnClosed();
    editor.goToCursorPositionVisible(25, 20);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectCompositeAutocompleteProposal("isEquals(Object o) : boolean Book");

    // then
    checkProposalDocumentationHTML(
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
    editor.typeTextIntoEditor(Keys.CONTROL.toString());
    editor.goToCursorPositionVisible(15, 4);
    editor.typeTextIntoEditor("UPDATE. ");

    editor.selectTabByName(APP_CLASS_NAME);
    editor.waitActive();
    editor.goToCursorPositionVisible(22, 12);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectCompositeAutocompleteProposal("BookImpl - multimodule.model");

    // then
    editor.waitProposalDocumentationHTML("UPDATE. Implementation of Book interface.");
  }

  @Test(groups = UNDER_REPAIR)
  public void shouldDisplayJavaDocOfJreClass() throws IOException {
    // when
    editor.waitActive();
    loader.waitOnClosed();
    editor.goToCursorPositionVisible(25, 20);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectCompositeAutocompleteProposal("hashCode() : int Object");

    // then
    checkProposalDocumentationHTML(
        "Returns a hash code value for the object. "
            + "This method is supported for the benefit of hash tables such as those provided by");
  }

  @Test
  public void shouldNotShowJavaDocIfExternalLibDoesNotExist() throws IOException {
    // when
    editor.waitActive();
    loader.waitOnClosed();
    editor.goToCursorPositionVisible(31, 23);
    editor.launchAutocompleteAndWaitContainer();
    editor.selectCompositeAutocompleteProposal("info(String msg) : void Logger");

    // then
    checkProposalDocumentationHTML(
        "<ul>\n"
            + "<li><p><strong>Parameters:</strong></p>\n"
            + "<ul>\n"
            + "<li><strong>msg</strong> the message string to be logged</li>\n"
            + "</ul>\n"
            + "</li>\n"
            + "</ul>");

    // when
    editor.closeAutocomplete();
    editor.typeTextIntoEditor(Keys.F4.toString());
    editor.waitActiveTabFileName("Logger.class");
    editor.selectTabByName(APP_CLASS_NAME);

    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(COMMAND_TO_REMOVE_SLF4J_ARTIFACTS);

    loader.waitOnClosed();
    editor.waitActive();
    editor.goToCursorPositionVisible(30, 20);
    editor.openJavaDocPopUp();

    // then
    assertFalse(editor.isTooltipPopupVisible());
  }

  private void checkProposalDocumentationHTML(String expectedText) {
    try {
      editor.waitProposalDocumentationHTML(expectedText);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/11743");
    }
  }
}
