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
package org.eclipse.che.selenium.refactor.packages;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class RenamePackageTest {

  private static String PROJECT_NAME = NameGenerator.generate("CheckRenamePackageProject-", 4);

  // TODO move all data from this fields into resources. See Utils.readContentFromFile, for example
  // RenameMethodInInterfaceTest
  private static String TEST0_P1_OUT =
      "package test0.p1;\n"
          + "/**\n"
          + " * This is in test0.r.\n"
          + " * @see test0.p1\n"
          + " * @see test0.p1.A\n"
          + " * @see test0.p1.A#A()\n"
          + " */\n"
          + "public class A {\n"
          + "}\n";

  private static String TEST1_P1_OUT =
      "package test1.p1;\n" + "public class A {\n" + "    test1.p1.A a;\n" + "}\n";

  private static String TEST2_FRED_OUT =
      "package test2.fred;\n"
          + "import test2.p1.*;\n"
          + "class A {\n"
          + "    test2.p1.A a;\n"
          + "    A a1;\n"
          + "}\n";

  private static String TEST3_R_OUT =
      "package test3.r;\n"
          + "import test3.r.r.*;\n"
          + "class A {\n"
          + "    test3.r.r.B a;\n"
          + "    A a1;\n"
          + "}\n";

  private static String TEST3_R_R_OUT =
      "package test3.r.r;\n" + "public class B {\n" + "    test3.r.r.B a;\n" + "}\n";

  private static String TEST4_Q_OUT = "package test4.q;\n" + "public class A {\n" + "}\n";

  private static String TEST4_FILE_OUT =
      "test4.q.B\n"
          + "test4.q.B.Q\n"
          + "test4.q.Bla\n"
          + "test4.q.q\n"
          + "\"test4.q.B\"\n"
          + "test4.q\n"
          + "test4.q.\n"
          + "\n"
          + "a.test4.r.p1.B\n"
          + "test4.rr.p1.B\n"
          + ".test4.r.p1.B\n"
          + "test4.r.p1p.B\n";

  private static String TEST5_P1_OUT =
      "//no ref update\n" + "package test5.p1;\n" + "class A {\n" + "    test5.r.A d;\n" + "}\n";

  private static String TEST6_P1_OUT =
      "//no ref update but update of textual match to test6.p1 and \"test6.p1\"\n"
          + "package test6.p1;\n"
          + "class A {\n"
          + "    test6.r.A d;\n"
          + "}\n";

  private static String TEST7_R_IN = "package test7.r;\n" + "class A {\n" + "}\n";

  private static String TEST7_Q_OUT = "package test7.q;\n" + "class A {\n" + "}\n";

  private static String TEST7_R_S_IN = "package test7.r.s;\n" + "public class B {\n" + "}\n";

  private static String TEST7_Q_S_OUT = "package test7.q.s;\n" + "public class B {\n" + "}\n";

  private static String TEST8_IN =
      "package java.lang.reflect;\n"
          + "\n"
          + "public class Klass extends AccessibleObject implements Type {\n"
          + "    Field f;\n"
          + "}\n";

  private static String TEST8_OUT =
      "package nonjava;\n"
          + "\n"
          + "import java.lang.reflect.AccessibleObject;\n"
          + "import java.lang.reflect.Field;\n"
          + "import java.lang.reflect.Type;\n"
          + "\n"
          + "public class Klass extends AccessibleObject implements Type {\n"
          + "    Field f;\n"
          + "}\n";

  private static String TEST9_HIERAR_IN =
      "package my;\n"
          + "\n"
          + "import my.a.ATest;\n"
          + "import my.b.B;\n"
          + "\n"
          + "public class MyA {\n"
          + "}\n";

  private static String TEST9_HIERAR_OUT =
      "package your;\n"
          + "\n"
          + "import your.a.ATest;\n"
          + "import your.b.B;\n"
          + "\n"
          + "public class MyA {\n"
          + "}\n";

  private static String TEST10_HIERAR_IN =
      "package m_y.pack;\n"
          + "\n"
          + "public class C {\n"
          + "/*\n"
          + "m_y.pack\n"
          + "m_y.pack.subpack\n"
          + "m_y.pack2\n"
          + "m_y.pack2.subpack\n"
          + "not.m_y.pack.subpack\n"
          + "notm_y.pack.subpack\n"
          + "notm_y.pack2.subpack\n"
          + "M_y.pack\n"
          + "*/\n"
          + "}\n";

  private static String TEST10_HIERAR_OUT =
      "package your.pack;\n"
          + "\n"
          + "public class C {\n"
          + "/*\n"
          + "your.pack\n"
          + "your.pack.subpack\n"
          + "m_y.pack2\n"
          + "m_y.pack2.subpack\n"
          + "not.m_y.pack.subpack\n"
          + "notm_y.pack.subpack\n"
          + "notm_y.pack2.subpack\n"
          + "M_y.pack\n"
          + "*/\n"
          + "}\n";

  private static String TEST11_DISABLED_IMPORT_IN =
      "package my_.pack;\n"
          + "\n"
          + "import my_.pack.C;\n"
          + "//import my_.Other;\n"
          + "\n"
          + "public class C {\n"
          + "}\n";

  private static String TEST11_DISABLED_IMPORT_OUT =
      "package your_.pack;\n"
          + "\n"
          + "import your_.pack.C;\n"
          + "\n"
          + "public class C {\n"
          + "}\n";

  private static String TEST12_RENAME_WITH_RESOURCE_IN =
      "mine.pack\n"
          + "mine.pack.\n"
          + "mine.pack.subpack\n"
          + "mine.pack2\n"
          + "mine.pack2.subpack\n"
          + "not.mine.pack.subpack\n"
          + "notmine.pack.subpack\n"
          + "notmine.pack2.subpack\n"
          + "Mine.pack\n";

  private static String TEST12_RENAME_WITH_RESOURCE_OUT =
      "mine\n"
          + "mine.\n"
          + "mine.subpack\n"
          + "mine.pack2\n"
          + "mine.pack2.subpack\n"
          + "not.mine.pack.subpack\n"
          + "notmine.pack.subpack\n"
          + "notmine.pack2.subpack\n"
          + "Mine.pack\n";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Refactor refactor;
  @Inject private Menu menu;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private MachineTerminal terminal;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/rename-package");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
    terminal.waitTerminalTab();
    expandTestProject(PROJECT_NAME);
  }

  @AfterMethod
  public void closeForm() {
    if (refactor.isWidgetOpened()) {
      refactor.clickCancelButtonRefactorForm();
    }

    if (editor.isAnyTabsOpened()) {
      editor.closeAllTabs();
    }
  }

  @Test
  public void checkRenamePackageForm00() {
    // check the 'Cancel' button
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples");
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples");
    loader.waitOnClosed();
    projectExplorer.launchRefactorByKeyboard();
    loader.waitOnClosed();
    refactor.waitRenamePackageFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.clickCancelButtonRefactorForm();
    refactor.waitRenamePackageFormIsClosed();
    loader.waitOnClosed();
  }

  @Test
  public void checkTest0() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test0/r");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test0/r/A.java");
    editor.waitActive();
    editor.waitTextNotPresentIntoEditor(TEST0_P1_OUT);
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java/test0/r");
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.Refactoring.REFACTORING,
        TestMenuCommandsConstants.Assistant.Refactoring.RENAME);

    refactor.waitRenamePackageFormIsOpen();
    loader.waitOnClosed();
    refactor.sendKeysIntoField("test0.p1");
    refactor.waitTextIntoNewNameField("test0.p1");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.setAndWaitStateRenameSubpackagesCheckbox(false);
    refactor.setAndWaitStateUpdateNonJavaFilesCheckbox(false);
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenamePackageFormIsClosed();
    projectExplorer.waitItemInvisibility(PROJECT_NAME + "/src/main/java/test0/r/A.java");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/test0/p1/A.java");
    editor.waitTextIntoEditor(TEST0_P1_OUT);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkTest1() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test1/r");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test1/r/A.java");
    editor.waitActive();
    editor.waitTextNotPresentIntoEditor(TEST1_P1_OUT);
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java/test1/r");
    projectExplorer.launchRefactorByKeyboard();
    refactor.waitRenamePackageFormIsOpen();
    refactor.sendKeysIntoField("test1.p1");
    refactor.waitTextIntoNewNameField("test1.p1");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.setAndWaitStateRenameSubpackagesCheckbox(false);
    refactor.setAndWaitStateUpdateNonJavaFilesCheckbox(false);
    refactor.clickOkButtonRefactorForm();
    refactor.waitRenamePackageFormIsClosed();
    projectExplorer.waitItemInvisibility(PROJECT_NAME + "/src/main/java/test1/r/A.java");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/test1/p1/A.java");
    editor.waitTextIntoEditor(TEST1_P1_OUT);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkTest2() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test2");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test2/fred");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test2/fred/A.java");
    editor.waitActive();
    editor.waitTextNotPresentIntoEditor(TEST2_FRED_OUT);
    editor.closeFileByNameWithSaving("A");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test2/r");
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java/test2/r");
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.Refactoring.REFACTORING,
        TestMenuCommandsConstants.Assistant.Refactoring.RENAME);
    refactor.waitRenamePackageFormIsOpen();
    loader.waitOnClosed();
    refactor.sendKeysIntoField("test2.p1");
    refactor.waitTextIntoNewNameField("test2.p1");
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.setAndWaitStateRenameSubpackagesCheckbox(true);
    refactor.setAndWaitStateUpdateNonJavaFilesCheckbox(false);
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenamePackageFormIsClosed();
    projectExplorer.waitItemInvisibility(PROJECT_NAME + "/src/main/java/test2/r");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/test2/p1");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test2/fred/A.java");
    loader.waitOnClosed();
    editor.waitTextIntoEditor(TEST2_FRED_OUT);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkTest3() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test3");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test3/fred");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test3/fred/A.java");
    editor.waitActive();
    editor.waitTextNotPresentIntoEditor(TEST3_R_OUT);
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java/test3/fred");
    projectExplorer.launchRefactorByKeyboard();
    refactor.waitRenamePackageFormIsOpen();
    refactor.sendKeysIntoField("test3.r");
    refactor.waitTextIntoNewNameField("test3.r");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.setAndWaitStateRenameSubpackagesCheckbox(false);
    refactor.setAndWaitStateUpdateNonJavaFilesCheckbox(false);
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenamePackageFormIsClosed();
    projectExplorer.waitItemInvisibility(PROJECT_NAME + "/src/main/java/test3/fred");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/test3/r");
    editor.waitTextIntoEditor(TEST3_R_OUT);
    editor.closeFileByNameWithSaving("A");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test3/r/r");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test3/r/r/B.java");
    editor.waitTextIntoEditor(TEST3_R_R_OUT);
    editor.closeFileByNameWithSaving("B");
  }

  @Test
  public void checkTest4() {
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/test4");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test4");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test4/r");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test4/Textfile.txt");
    editor.waitTextNotPresentIntoEditor(TEST4_FILE_OUT);
    editor.closeFileByNameWithSaving("Textfile.txt");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test4/r/p1");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test4/r/p1/A.java");
    editor.waitTextNotPresentIntoEditor(TEST4_Q_OUT);
    editor.closeFileByNameWithSaving("A");
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java/test4/r/p1");
    projectExplorer.launchRefactorByKeyboard();
    refactor.waitRenamePackageFormIsOpen();
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.setAndWaitStateRenameSubpackagesCheckbox(true);
    refactor.setAndWaitStateUpdateNonJavaFilesCheckbox(true);
    refactor.clearFieldAndSendKeys("test4.q");
    refactor.waitTextIntoNewNameField("test4.q");
    loader.waitOnClosed();
    refactor.typeAndWaitFileNamePatterns("*.txt");
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    refactor.waitRenamePackageFormIsClosed();
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/test4/r");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/test4/q");
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java/test4/r");
    projectExplorer.waitItemInvisibility(PROJECT_NAME + "/src/main/java/test4/r/p1");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test4/q/A.java");
    loader.waitOnClosed();
    editor.waitTextIntoEditor(TEST4_Q_OUT);
    editor.closeFileByNameWithSaving("A");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test4/Textfile.txt");
    editor.waitTextIntoEditor(TEST4_FILE_OUT);
    editor.closeFileByNameWithSaving("Textfile.txt");
  }

  @Test
  public void checkTest5() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test5/r");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test5/r/A.java");
    editor.waitActive();
    editor.waitTextNotPresentIntoEditor(TEST5_P1_OUT);
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java/test5/r");
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.Refactoring.REFACTORING,
        TestMenuCommandsConstants.Assistant.Refactoring.RENAME);

    refactor.waitRenamePackageFormIsOpen();
    loader.waitOnClosed();
    refactor.sendKeysIntoField("test5.p1");
    refactor.waitTextIntoNewNameField("test5.p1");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateNonJavaFilesCheckbox(false);
    refactor.setAndWaitStateUpdateReferencesCheckbox(false);
    refactor.setAndWaitStateRenameSubpackagesCheckbox(false);
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenamePackageFormIsClosed();
    editor.waitTextIntoEditor(TEST5_P1_OUT);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkTest6() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test6/r");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test6/r/A.java");
    editor.waitActive();
    editor.waitTextNotPresentIntoEditor(TEST6_P1_OUT);
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java/test6/r");
    projectExplorer.launchRefactorByKeyboard();
    refactor.waitRenamePackageFormIsOpen();
    loader.waitOnClosed();
    refactor.sendKeysIntoField("test6.p1");
    refactor.waitTextIntoNewNameField("test6.p1");
    refactor.setAndWaitStateUpdateReferencesCheckbox(false);
    refactor.setAndWaitStateRenameSubpackagesCheckbox(false);
    refactor.setAndWaitStateUpdateNonJavaFilesCheckbox(false);
    refactor.setAndWaitStateCommentsAndStringsCheckbox(true);
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenamePackageFormIsClosed();
    editor.waitTextIntoEditor(TEST6_P1_OUT);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkTest7() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test7/r");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test7/r/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(TEST7_R_IN);
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test7/r/s");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test7/r/s/B.java");
    editor.waitActive();
    editor.waitTextIntoEditor(TEST7_R_S_IN);
    editor.closeFileByNameWithSaving("A");
    editor.closeFileByNameWithSaving("B");
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java/test7/r");
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.Refactoring.REFACTORING,
        TestMenuCommandsConstants.Assistant.Refactoring.RENAME);

    refactor.waitRenamePackageFormIsOpen();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.setAndWaitStateRenameSubpackagesCheckbox(true);
    refactor.setAndWaitStateUpdateNonJavaFilesCheckbox(false);
    loader.waitOnClosed();
    refactor.clearFieldAndSendKeys("test7.q");
    refactor.waitTextIntoNewNameField("test7.q");
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenamePackageFormIsClosed();
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/test7/q");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test7/q/A.java");
    editor.waitTextIntoEditor(TEST7_Q_OUT);
    editor.closeFileByNameWithSaving("A");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/test7/q/s");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test7/q/s");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test7/q/s/B.java");
    editor.waitTextIntoEditor(TEST7_Q_S_OUT);
    editor.closeFileByNameWithSaving("B");
  }

  @Test
  public void checkTest8() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/java/lang/reflect");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/java/lang/reflect/Klass.java");
    editor.waitActive();
    editor.waitTextIntoEditor(TEST8_IN);
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java/java/lang/reflect");
    projectExplorer.launchRefactorByKeyboard();
    refactor.waitRenamePackageFormIsOpen();
    loader.waitOnClosed();

    refactor.sendKeysIntoField("nonjava");
    refactor.waitTextIntoNewNameField("nonjava");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.setAndWaitStateRenameSubpackagesCheckbox(true);
    refactor.setAndWaitStateUpdateNonJavaFilesCheckbox(false);
    refactor.clickPreviewButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRefactorPreviewFormIsOpened();
    loader.waitOnClosed();
    refactor.clickOkButtonPreviewForm();
    loader.waitOnClosed();
    refactor.waitRenamePackageFormIsClosed();
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/nonjava");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/nonjava/Klass.java");
    editor.waitActive();
    editor.waitTextIntoEditor(TEST8_OUT);
    editor.closeFileByNameWithSaving("Klass");
  }

  @Test
  public void checkTestHierarchical9_1() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/my");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/my/MyA.java");
    editor.waitActive();
    editor.waitTextIntoEditor(TEST9_HIERAR_IN);
    editor.closeFileByNameWithSaving("MyA");
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java/my");
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.Refactoring.REFACTORING,
        TestMenuCommandsConstants.Assistant.Refactoring.RENAME);

    refactor.waitRenamePackageFormIsOpen();
    refactor.sendKeysIntoField("your");
    refactor.waitTextIntoNewNameField("your");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.setAndWaitStateRenameSubpackagesCheckbox(true);
    refactor.setAndWaitStateUpdateNonJavaFilesCheckbox(false);
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenamePackageFormIsClosed();
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/your");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/your/MyA.java");
    editor.waitTextIntoEditor(TEST9_HIERAR_OUT);
    editor.closeFileByNameWithSaving("MyA");
  }

  @Test
  public void checkTestHierarchical10() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/m_y/pack");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/m_y/pack/C.java");
    editor.waitActive();
    editor.waitTextIntoEditor(TEST10_HIERAR_IN);
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java/m_y/pack");
    projectExplorer.launchRefactorByKeyboard();
    refactor.waitRenamePackageFormIsOpen();
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.setAndWaitStateRenameSubpackagesCheckbox(true);
    refactor.setAndWaitStateUpdateNonJavaFilesCheckbox(false);
    refactor.setAndWaitStateCommentsAndStringsCheckbox(true);
    loader.waitOnClosed();
    refactor.clearFieldAndSendKeys("your.pack");
    refactor.waitTextIntoNewNameField("your.pack");
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenamePackageFormIsClosed();
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/your/pack");
    editor.waitTextIntoEditor(TEST10_HIERAR_OUT);
    editor.closeFileByNameWithSaving("C");
  }

  @Test
  public void checkTestDisableImport11() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/my_/pack");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/my_/pack/C.java");
    editor.waitActive();
    editor.waitTextIntoEditor(TEST11_DISABLED_IMPORT_IN);
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java/my_/pack");
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.Refactoring.REFACTORING,
        TestMenuCommandsConstants.Assistant.Refactoring.RENAME);

    refactor.waitRenamePackageFormIsOpen();
    loader.waitOnClosed();
    refactor.sendKeysIntoField("your_.pack");
    refactor.waitTextIntoNewNameField("your_.pack");
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.setAndWaitStateRenameSubpackagesCheckbox(true);
    refactor.setAndWaitStateUpdateNonJavaFilesCheckbox(false);
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenamePackageFormIsClosed();
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/your_/pack");
    editor.waitTextIntoEditor(TEST11_DISABLED_IMPORT_OUT);
    editor.closeFileByNameWithSaving("C");
  }

  @Test
  public void checkTestPackageRenameWithResource12() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/mine/pack");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/mine/pack/Textfile.txt");
    editor.waitActive();
    editor.waitTextIntoEditor(TEST12_RENAME_WITH_RESOURCE_IN);
    editor.closeFileByNameWithSaving("Textfile.txt");
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java/mine/pack");
    projectExplorer.launchRefactorByKeyboard();
    refactor.waitRenamePackageFormIsOpen();
    loader.waitOnClosed();
    refactor.sendKeysIntoField("mine");
    refactor.waitTextIntoNewNameField("mine");
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.setAndWaitStateRenameSubpackagesCheckbox(false);
    refactor.setAndWaitStateUpdateNonJavaFilesCheckbox(true);
    refactor.typeAndWaitFileNamePatterns("*.txt");
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    refactor.waitRenamePackageFormIsClosed();
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/mine");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/mine/Textfile.txt");
    editor.waitTextIntoEditor(TEST12_RENAME_WITH_RESOURCE_OUT);
    editor.closeFileByNameWithSaving("Textfile.txt");
  }

  private void expandTestProject(String projectName) {
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(projectName + "/src");
    projectExplorer.waitItem(projectName + "/src/main");
    projectExplorer.openItemByPath(projectName + "/src/main");
    projectExplorer.waitItem(projectName + "/src/main/java");
    projectExplorer.openItemByPath(projectName + "/src/main/java");
    projectExplorer.waitItem(projectName + "/src/main/java/org/eclipse/qa/examples");
    loader.waitOnClosed();
  }
}
