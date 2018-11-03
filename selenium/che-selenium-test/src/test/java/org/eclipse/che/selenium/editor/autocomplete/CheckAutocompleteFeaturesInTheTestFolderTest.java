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

import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * //
 *
 * @author Musienko Maxim
 * @author Aleksandr Shmaraev
 */
public class CheckAutocompleteFeaturesInTheTestFolderTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate("CheckAuthoCompleteInTheTestFolder_", 4);
  private static final String TEST_CLASS = "AppTest.java";
  private static final String TAB_TITLE = "AppTest";
  private static final String PATH_TO_TEST_FOLDER =
      PROJECT_NAME + "/src/test/java/com/codenvy/example/java/";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/console-java-with-html-file");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);

    ide.open(workspace);
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void checkAutocompleteFeaturesInTheTestFolderTest() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);

    projectExplorer.clickOnProjectExplorerTab();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);

    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    projectExplorer.openItemByPath(PATH_TO_TEST_FOLDER + TEST_CLASS);
    editor.waitTabIsPresent(TAB_TITLE);

    checkOpenDeclaration();
    checkAutocompletion();
    checkJavadoc();
  }

  private void checkOpenDeclaration() {
    final String expectedTabTitle = "Test.class";
    final String expectedContent =
        "package junit.framework;\n"
            + "\n"
            + "/**\n"
            + " * A <em>Test</em> can be run and collect its results.\n"
            + " *\n"
            + " * @see TestResult\n"
            + " */\n"
            + "public interface Test {\n"
            + " /**\n"
            + "  * Counts the number of test cases that will be run by this test.\n"
            + "  */\n"
            + " public abstract int countTestCases();\n"
            + " /**\n"
            + "  * Runs a test and collects its result in a TestResult instance.\n"
            + "  */\n"
            + " public abstract void run(TestResult result);\n"
            + "}";

    // prepare file
    editor.waitActive();
    editor.goToCursorPositionVisible(36, 21);
    editor.waitActive();
    editor.waitSpecifiedValueForLineAndChar(36, 21);

    // check open declaration
    editor.typeTextIntoEditor(Keys.F4.toString());
    editor.waitTabIsPresent(expectedTabTitle);
    editor.waitTextIntoEditor(expectedContent);
    editor.closeFileByNameWithSaving(expectedTabTitle);
  }

  private void checkAutocompletion() {
    final String[] autocompleteItems = {
      "Test", "TestSuite", "TestCollector", "TestListener", "TestFailure"
    };

    final String textBeforeAutocomplete =
        "    public AppTest(String testName) {\n"
            + "        super(testName);\n"
            + "        Test\n"
            + "    }";

    final String textAfterAutocomplete =
        "    public AppTest(String testName) {\n"
            + "        super(testName);\n"
            + "        TestCase\n"
            + "    }";

    final String codeWithoutErrors =
        "    public AppTest(String testName) {\n"
            + "        super(testName);\n"
            + "        TestCase testCase;\n"
            + "    }";

    // prepare text and launch autocomplete
    editor.waitActive();
    editor.goToCursorPositionVisible(30, 25);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitSpecifiedValueForLineAndChar(31, 9);
    editor.typeTextIntoEditor("Test");
    editor.waitTextIntoEditor(textBeforeAutocomplete);
    editor.launchAutocomplete();

    // check autocomplete proposals
    for (String autocompleteItem : autocompleteItems) {
      editor.waitProposalIntoAutocompleteContainer(autocompleteItem);
    }

    // check applying of the autocomplete suggestion
    editor.enterAutocompleteProposal("Case - junit.framework");
    editor.waitTextIntoEditor(textAfterAutocomplete);
    editor.typeTextIntoEditor(" testCase;");
    editor.waitTextIntoEditor(codeWithoutErrors);
  }

  private void checkJavadoc() {
    final String tabTitle = "AppTest";
    final String expectedTextInJavaDoc =
        "The class String includes methods for examining individual characters of the sequence, for comparing strings, for searching strings, for extracting substrings, and for creating a copy of a string with all characters translated to uppercase or to lowercase.";

    editor.waitTabIsPresent(tabTitle);
    editor.selectTabByName(tabTitle);
    editor.waitTabFocusing(0, tabTitle);
    editor.waitActive();
    editor.goToCursorPositionVisible(29, 21);
    editor.openJavaDocPopUp();
    try {
      editor.waitJavaDocPopUpOpened();
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known random failure https://github.com/eclipse/che/issues/11735", ex);
    }

    editor.checkTextToBePresentInJavaDocPopUp(expectedTextInJavaDoc);
  }
}
