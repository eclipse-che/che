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
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
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
  private static final String tesClass = "AppTest.java";
  private final String pathToClassInTstFolder =
      PROJECT_NAME + "/src/test/java/com/codenvy/example/java/";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/console-java-with-html-file");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test
  public void checkAutocompleteFeaturesInTheTestFolderTest() {
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    projectExplorer.openItemByPath(pathToClassInTstFolder + tesClass);
    checkOpenDeclaration();
    checkAutocompletion();
    checkJavadoc();
  }

  private void checkOpenDeclaration() {
    editor.waitActive();
    editor.goToCursorPositionVisible(35, 21);
    editor.waitActive();
    editor.waitSpecifiedValueForLineAndChar(35, 21);
    editor.typeTextIntoEditor(Keys.F4.toString());
    editor.waitTabIsPresent("Test");
    String expectedContent =
        "\n"
            + " // Failed to get sources. Instead, stub sources have been generated.\n"
            + " // Implementation of methods is unavailable.\n"
            + "package junit.framework;\n"
            + "public interface Test {\n"
            + "\n"
            + "    public int countTestCases();\n"
            + "\n"
            + "    public void run(junit.framework.TestResult arg0);\n"
            + "\n"
            + "}\n";

    editor.waitTextIntoEditor(expectedContent);
    editor.closeFileByNameWithSaving("Test");
  }

  private void checkAutocompletion() {
    editor.returnFocusInCurrentLine();
    editor.goToCursorPositionVisible(29, 25);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitSpecifiedValueForLineAndChar(30, 9);
    editor.typeTextIntoEditor("Test");
    editor.launchAutocomplete();
    String[] autocompleteItems = {
      "Test", "TestSuite", "TestCollector", "TestListener", "TestFailure"
    };
    for (String autocompleteItem : autocompleteItems) {
      editor.waitTextIntoAutocompleteContainer(autocompleteItem);
    }
    editor.enterAutocompleteProposal("TestCase");
    editor.waitTextIntoEditor(
        "    public AppTest(String testName) {\n"
            + "        super(testName);\n"
            + "        TestCase\n"
            + "    }");
    editor.typeTextIntoEditor(" testCase;");
  }

  private void checkJavadoc() {
    editor.goToCursorPositionVisible(28, 21);
    editor.openJavaDocPopUp();
    String expectedTextInJavaDoc =
        "The String class represents character strings. "
            + "All string literals in Java programs, such as \"abc\", are implemented as instances of this class.";
    editor.checkTextToBePresentInJavaDocPopUp(expectedTextInJavaDoc);
  }
}
