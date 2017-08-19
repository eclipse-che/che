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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.FILE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.NEW;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SUBVERSION;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SVN_ADD;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SVN_COMMIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SVN_DELETE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SVN_STATUS;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.IMPORT_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.provider.TestSvnPasswordProvider;
import org.eclipse.che.selenium.core.provider.TestSvnRepo1Provider;
import org.eclipse.che.selenium.core.provider.TestSvnUsernameProvider;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.subversion.Subversion;
import org.testng.annotations.Test;

/**
 * @author Anton Korneta
 * @author Andrey Chizhikov
 */
public class CommitTest {
  @Inject private TestSvnRepo1Provider svnRepo1UrlProvider;
  @Inject private TestSvnUsernameProvider svnUsernameProvider;
  @Inject private TestSvnPasswordProvider svnPasswordProvider;
  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Subversion subversion;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Wizard wizard;
  @Inject private ImportProjectFromLocation importProjectFromLocation;
  @Inject private AskForValueDialog askForValueDialog;

  private static final String PROJECT_NAME = NameGenerator.generate("CommitTestProject", 6);
  private static final String COMMIT_FOLDER_PATH = PROJECT_NAME + "/trunk/commit-test";
  private static final String COMMIT_TEST_FILE = "commit-test.txt";
  private static final String COMMIT_MESSAGE = "commit-test";

  @Test
  public void commitTest() throws Exception {
    ide.open(testWorkspace);

    loader.waitOnClosed();

    projectExplorer.waitProjectExplorer();
    menu.runCommand(WORKSPACE, IMPORT_PROJECT);
    subversion.waitAndTypeImporterAsSvnInfo(
        svnRepo1UrlProvider.get(),
        PROJECT_NAME,
        svnUsernameProvider.get(),
        svnPasswordProvider.get());
    importProjectFromLocation.waitMainFormIsClosed();
    wizard.waitOpenProjectConfigForm();
    wizard.clickSaveButton();
    wizard.waitCloseProjectConfigForm();

    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAME);

    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.selectItem(COMMIT_FOLDER_PATH + "/" + COMMIT_TEST_FILE);
    projectExplorer.openItemByPath(COMMIT_FOLDER_PATH + "/" + COMMIT_TEST_FILE);
    editor.waitActiveEditor();
    editor.typeTextIntoEditor("changes for commit");
    menu.runAndWaitCommand(SUBVERSION, SVN_STATUS);
    loader.waitOnClosed();
    menu.runAndWaitCommand(SUBVERSION, SVN_COMMIT);
    subversion.waitSvnCommitFormOpened();
    subversion.typeSvnCommitMessage(COMMIT_MESSAGE);
    subversion.clickSvnCommitButtonCommit();
    subversion.waitSvnCommitFormClosed();

    projectExplorer.selectItem(COMMIT_FOLDER_PATH + "/" + COMMIT_TEST_FILE);
    menu.runAndWaitCommand(SUBVERSION, SVN_DELETE);
    loader.waitOnClosed();
    projectExplorer.selectItem(COMMIT_FOLDER_PATH);
    menu.runCommand(TestMenuCommandsConstants.Project.PROJECT, NEW, FILE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(COMMIT_TEST_FILE);
    askForValueDialog.clickOkBtn();
    projectExplorer.selectItem(COMMIT_FOLDER_PATH + "/" + COMMIT_TEST_FILE);
    menu.runAndWaitCommand(SUBVERSION, SVN_ADD);
    loader.waitOnClosed();
    menu.runAndWaitCommand(SUBVERSION, SVN_COMMIT);
    subversion.waitSvnCommitFormOpened();
    subversion.typeSvnCommitMessage("clean up for commit test");
    subversion.clickSvnCommitButtonCommit();
    subversion.waitSvnCommitFormClosed();
  }
}
