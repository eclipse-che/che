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
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Igor Vinokur
 * @author Andrey Chizhikov
 */
public class JavaDocPopupTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(JavaDocPopupTest.class.getSimpleName(), 4);
  private static final String CLASS_NAME_TEXT = "org.eclipse.qa.examples.AppController";
  private static final String PATH_TO_FILES =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples";

  private static final String JAVA_DOC_FOR_TEST_CLASS =
      "org.eclipse.qa.examples.TestClass\n" + "\n" + "Hello";
  private static final String JAVA_DOC_FOR_OBJECT =
      "java.lang.Object\n"
          + "\n"
          + "Class Object is the root of the class hierarchy. Every class has Object as a superclass. "
          + "All objects, including arrays, implement the methods of this class.\n"
          + "Since:\n"
          + "JDK1.0\n"
          + "Author:\n"
          + "unascribed\n"
          + "See Also:\n"
          + "java.lang.Class";
  private static final String ANNOTATION_TEXT =
      "java.lang.Override\n"
          + "\n"
          + "Indicates that a method declaration is intended to override a method declaration "
          + "in a supertype. If a method is annotated with this annotation type compilers are"
          + " required to generate an error message unless at least one of the following "
          + "conditions hold:\n"
          + "The method does override or implement a method declared in a supertype.\n"
          + "The method has a signature that is override-equivalent to that of any public"
          + " method declared in Object.\n"
          + "Since:\n"
          + "1.5\n"
          + "Author:\n"
          + "Peter von der Ahé\n"
          + "Joshua Bloch\n"
          + "@jls\n"
          + "9.6.1.4 @Override";
  private static final String CONSTRUCTOR_TEXT =
      "org.eclipse.qa.examples.AppController.AppController()";
  private static final String CLASS_TEXT =
      "java.lang.Exception\n"
          + "\n"
          + "The class Exception and its subclasses are a form of Throwable that"
          + " indicates conditions that a reasonable application might want to catch.\n"
          + "The class Exception and any subclasses that are not also subclasses "
          + "of RuntimeException are checked exceptions. Checked exceptions need "
          + "to be declared in a method or constructor's throws clause if they can"
          + " be thrown by the execution of the method or constructor and propagate"
          + " outside the method or constructor boundary.\n"
          + "Since:\n"
          + "JDK1.0\n"
          + "Author:\n"
          + "Frank Yellin\n"
          + "See Also:\n"
          + "java.lang.Error\n"
          + "@jls\n"
          + "11.2 Compile-Time Checking of Exceptions";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestProjectServiceClient projectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
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
  public void javaDocPopupTest() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    consoles.closeProcessesArea();
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitItem(PATH_TO_FILES + "/AppController.java");
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    loader.waitOnClosed();
    // Class javadoc popup
    editor.goToCursorPositionVisible(26, 105);

    editor.openJavaDocPopUp();
    editor.waitJavaDocPopUpOpened();
    editor.checkTextToBePresentInJavaDocPopUp(CLASS_TEXT);

    editor.selectTabByName("AppController");
    editor.waitJavaDocPopUpClosed();

    // Annotation javadoc popup
    editor.typeTextIntoEditor(Keys.CONTROL.toString());
    editor.waitActive();
    editor.goToCursorPositionVisible(25, 6);

    editor.openJavaDocPopUp();
    editor.waitJavaDocPopUpOpened();
    editor.checkTextToBePresentInJavaDocPopUp(ANNOTATION_TEXT);

    editor.typeTextIntoEditor(Keys.ESCAPE.toString());
    editor.waitJavaDocPopUpClosed();
    editor.typeTextIntoEditor(Keys.CONTROL.toString());

    // Class name javadoc popup
    editor.goToCursorPositionVisible(22, 17);
    editor.openJavaDocPopUp();
    editor.waitJavaDocPopUpOpened();
    editor.checkTextToBePresentInJavaDocPopUp(CLASS_NAME_TEXT);

    editor.selectTabByName("AppController");
    editor.waitJavaDocPopUpClosed();
    editor.typeTextIntoEditor(Keys.CONTROL.toString());

    // Class constructor name javadoc popup
    editor.setCursorToLine(24);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("public AppController() {}");
    editor.typeTextIntoEditor(Keys.ENTER.toString());

    editor.goToCursorPositionVisible(25, 15);
    editor.openJavaDocPopUp();
    editor.checkTextToBePresentInJavaDocPopUp(CONSTRUCTOR_TEXT);

    editor.selectTabByName("AppController");
    editor.waitJavaDocPopUpClosed();
    editor.typeTextIntoEditor(Keys.CONTROL.toString());

    createClass("TestClass", PATH_TO_FILES);
    editor.setCursorToLine(2);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("/**");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.UP.toString());
    editor.typeTextIntoEditor("Hello");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("<script>alert('Hello')</script>");
    editor.closeAllTabsByContextMenu();
    projectExplorer.openItemByPath(PATH_TO_FILES + "/AppController.java");
    editor.setCursorToLine(24);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("TestClass abc = new TestClass(); Object testObject = new Object();");
    editor.typeTextIntoEditor(Keys.HOME.toString());

    editor.goToCursorPositionVisible(25, 5);
    editor.openJavaDocPopUp();
    editor.checkTextToBePresentInJavaDocPopUp(JAVA_DOC_FOR_TEST_CLASS);

    editor.selectTabByName("AppController");
    editor.waitJavaDocPopUpClosed();
    editor.typeTextIntoEditor(Keys.CONTROL.toString());

    editor.goToCursorPositionVisible(25, 35);
    editor.openJavaDocPopUp();
    editor.waitJavaDocPopUpOpened();
    editor.checkTextToBePresentInJavaDocPopUp(JAVA_DOC_FOR_OBJECT);
  }

  private void createClass(String className, String pathParent) {
    projectExplorer.waitAndSelectItem(pathParent);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.JAVA_CLASS);
    loader.waitOnClosed();
    askForValueDialog.createJavaFileByNameAndType(className, AskForValueDialog.JavaFiles.CLASS);
    loader.waitOnClosed();
    projectExplorer.waitVisibilityByName(className + ".java");
  }
}
