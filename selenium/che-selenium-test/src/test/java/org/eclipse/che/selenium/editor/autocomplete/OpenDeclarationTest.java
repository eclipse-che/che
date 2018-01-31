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
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Igor Vinokur
 * @author Aleksandr Shmaraev
 */
public class OpenDeclarationTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(OpenDeclarationTest.class.getSimpleName(), 4);

  private String expectedTextBeforeDownloadSources = "";
  private String expectedTextAfterDownloadSources = "";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private MavenPluginStatusBar mavenPluginStatusBar;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private Events events;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void prepare() throws Exception {
    URL resources = OpenDeclarationTest.class.getResource("expected-test-before-download-sources");
    List<String> expectedBeforeTextList =
        Files.readAllLines(Paths.get(resources.toURI()), Charset.forName("UTF-8"));
    for (String bufer : expectedBeforeTextList) {
      expectedTextBeforeDownloadSources += bufer + '\n';
    }

    resources = OpenDeclarationTest.class.getResource("expected-test-after-download-sources");
    List<String> expectedAfterTextList =
        Files.readAllLines(Paths.get(resources.toURI()), Charset.forName("UTF-8"));
    for (String bufer : expectedAfterTextList) {
      expectedTextAfterDownloadSources += bufer + '\n';
    }

    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void navigateToSourceTest() throws Exception {
    projectExplorer.waitItem(PROJECT_NAME);
    events.clickEventLogBtn();
    mavenPluginStatusBar.waitClosingInfoPanel();
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    loader.waitOnClosed();
    editor.selectTabByName("AppController");
    editor.setCursorToLine(21);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("import sun.net.spi.nameservice.dns.DNSNameServiceDescriptor;");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.setCursorToLine(26);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(
        "DNSNameServiceDescriptor descriptor = new DNSNameServiceDescriptor();");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("String sdf = descriptor.getProviderName();");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    loader.waitOnClosed();

    editor.goToCursorPositionVisible(27, 10);
    editor.typeTextIntoEditor(Keys.F4.toString());
    editor.waitTabIsPresent("DNSNameServiceDescriptor");
    editor.waitActive();
    editor.setCursorToLine(5);
    editor.waitTextElementsActiveLine("DNSNameServiceDescriptor");
    editor.closeFileByNameWithSaving("DNSNameServiceDescriptor");
    editor.waitTabIsNotPresent("DNSNameServiceDescriptor");
    editor.selectTabByName("AppController");
    editor.goToCursorPositionVisible(28, 39);
    editor.typeTextIntoEditor(Keys.F4.toString());
    editor.waitTabIsPresent("DNSNameServiceDescriptor");
    editor.setCursorToLine(11);
    editor.waitTextElementsActiveLine("getProviderName");
    editor.closeFileByNameWithSaving("DNSNameServiceDescriptor");

    // check an ability to download source
    editor.selectTabByName("AppController");
    editor.goToCursorPositionVisible(31, 12);
    editor.typeTextIntoEditor(Keys.F4.toString());
    editor.waitTabIsPresent("ModelAndView");
    // editor.waitTextIntoEditor(expectedTextBeforeDownloadSources);
    editor.clickOnDownloadSourcesLink();
    loader.waitOnClosed();
    // editor.waitTextIntoEditor(expectedTextAfterDownloadSources);

    editor.closeFileByNameWithSaving("ModelAndView");

    // check go to class
    editor.selectTabByName("AppController");
    editor.goToCursorPositionVisible(31, 12);
    editor.typeTextIntoEditor(Keys.F4.toString());
    editor.waitTabIsPresent("ModelAndView");
    editor.waitTextElementsActiveLine("ModelAndView");
    editor.waitSpecifiedValueForLineAndChar(44, 14);
    editor.closeFileByNameWithSaving("ModelAndView");

    // Check go to method
    editor.selectTabByName("AppController");
    editor.goToCursorPositionVisible(44, 16);
    editor.typeTextIntoEditor(Keys.F4.toString());
    editor.waitTabIsPresent("ModelAndView");
    editor.waitTextElementsActiveLine("addObject");
    editor.waitSpecifiedValueForLineAndChar(226, 22);

    // Check go to inner method
    editor.goToCursorPositionVisible(227, 9);
    editor.typeTextIntoEditor(Keys.F4.toString());
    editor.waitTabIsPresent("ModelAndView");
    editor.waitTextElementsActiveLine("getModelMap()");
    editor.waitSpecifiedValueForLineAndChar(203, 18);
  }
}
