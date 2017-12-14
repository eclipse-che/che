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
package org.eclipse.che.selenium.subversion;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SUBVERSION;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SVN_COMMIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SVN_VIEW_DIFF;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.IMPORT_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.provider.TestSvnPasswordProvider;
import org.eclipse.che.selenium.core.provider.TestSvnRepo2Provider;
import org.eclipse.che.selenium.core.provider.TestSvnUsernameProvider;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.subversion.Subversion;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Aleksandr Shmaraev
 * @author Andrey Chizhikov
 */
public class DiffViewTest {

  private static final String PROJECT_NAME = "CheckDiffView";
  private static final String FILE_NAME_1 = "readme";
  private static final String FILE_NAME_2 = "document.html";
  private static final String FOLDER_NAME = "trunk/diff-view-test";
  private static final Logger LOG = LoggerFactory.getLogger(DiffViewTest.class);
  private static final String DIFF_MESS_1 =
      "Index: trunk/diff-view-test/readme\n"
          + "===================================================================\n"
          + "--- trunk/diff-view-test/readme (revision 1)\n"
          + "+++ trunk/diff-view-test/readme (working copy)\n"
          + "@@ -1,2 +1,3 @@\n"
          + "+###### qa\n"
          + " *****Update*****\n"
          + "   ++++ uuuuu+++++\n"
          + "\\ No newline at end of file";
  private static final String DIFF_MESS_2 =
      "Index: trunk/diff-view-test/document.html\n"
          + "===================================================================\n"
          + "--- trunk/diff-view-test/document.html (revision 1)\n"
          + "+++ trunk/diff-view-test/document.html (working copy)\n"
          + "@@ -1,3 +1,4 @@\n"
          + "+<!*** change content ***>\n"
          + " -<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \n"
          + " -  \"http://www.w3.org/TR/html4/strict.dtd\">\n"
          + " -<html>";

  @Inject private Ide ide;
  @Inject private TestWorkspace ws;
  @Inject private TestSvnRepo2Provider svnRepo2urlProvider;
  @Inject private TestSvnUsernameProvider svnUsernameProvider;
  @Inject private TestSvnPasswordProvider svnPasswordProvider;

  @Inject private Menu menu;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Wizard wizard;
  @Inject private ImportProjectFromLocation importProjectFromLocation;
  @Inject private Loader loader;
  @Inject private Subversion subversion;
  @Inject private CodenvyEditor editor;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(ws);
  }

  @Test
  public void checkDiffView() throws InterruptedException {
    projectExplorer.waitProjectExplorer();
    menu.runCommand(WORKSPACE, IMPORT_PROJECT);
    subversion.waitAndTypeImporterAsSvnInfo(
        svnRepo2urlProvider.get(),
        PROJECT_NAME,
        svnUsernameProvider.get(),
        svnPasswordProvider.get());
    importProjectFromLocation.waitMainFormIsClosed();

    wizard.waitOpenProjectConfigForm();
    wizard.clickSaveButton();
    wizard.waitCloseProjectConfigForm();

    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);

    // Change the file 'readme'
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME + "/trunk");
    projectExplorer.openItemByPath(PROJECT_NAME + "/trunk/diff-view-test");
    projectExplorer.openItemByPath(PROJECT_NAME + "/trunk/diff-view-test/readme");
    editor.waitActiveEditor();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.ARROW_UP.toString());
    editor.typeTextIntoEditor("###### qa");
    editor.waitTextIntoEditor("###### qa");

    // Change the file 'document.html'
    projectExplorer.openItemByPath(PROJECT_NAME + "/trunk/diff-view-test/document.html");
    editor.waitActiveEditor();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.ARROW_UP.toString());
    editor.typeTextIntoEditor("<!*** change content ***>");
    editor.waitTextIntoEditor("<!*** change content ***>");

    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(SUBVERSION, SVN_COMMIT);
    subversion.waitSvnCommitFormOpened();
    subversion.clickSvnCommitDiffButton(FILE_NAME_1, FOLDER_NAME);
    loader.waitOnClosed();
    subversion.waitSvnCommitFormOpened();

    try {
      subversion.waitTextDiffView(DIFF_MESS_1);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("https://github.com/eclipse/che/issues/6452", ex);
    }
    subversion.clickSvnCommitClosedButtonDiffView();

    // Check the diff view 'document.html' in the svn commit form
    subversion.clickSvnCommitDiffButton(FILE_NAME_2, FOLDER_NAME);
    loader.waitOnClosed();
    subversion.waitSvnCommitFormOpened();
    subversion.waitTextDiffView(DIFF_MESS_2);
    subversion.clickSvnCommitClosedButtonDiffView();
    subversion.waitSvnCommitFormOpened();
    subversion.clickSvnCommiCancelButtont();

    // Check the 'View Diff' in the svn status bar
    projectExplorer.selectItem(PROJECT_NAME + "/trunk/diff-view-test/readme");
    menu.runCommand(SUBVERSION, SVN_VIEW_DIFF);
    loader.waitOnClosed();

    subversion.waitSvnInfoPanelWithMessage(DIFF_MESS_1);
    projectExplorer.selectItem(PROJECT_NAME + "/trunk/diff-view-test/document.html");
    menu.runCommand(SUBVERSION, SVN_VIEW_DIFF);
    loader.waitOnClosed();

    subversion.waitSvnInfoPanelWithMessage(DIFF_MESS_2);
  }
}
