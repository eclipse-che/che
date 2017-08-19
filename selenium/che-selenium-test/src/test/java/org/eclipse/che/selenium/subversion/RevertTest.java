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
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SUBVERSION;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SVN_ADD;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SVN_DELETE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SVN_REVERT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SVN_STATUS;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.IMPORT_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Anton Korneta
 * @author Andrey Chizhikov
 */
public class RevertTest {
  private static final String PROJECT_NAME = NameGenerator.generate("RevertTestProject", 6);
  private static final String REVERT_FOLDER = PROJECT_NAME + "/tags/revert-test";
  private static final String REVERT_FILE = REVERT_FOLDER + "/revert-file.txt";
  private static final Logger LOG = LoggerFactory.getLogger(RevertTest.class);

  @Inject private Ide ide;
  @Inject private TestWorkspace ws;
  @Inject private TestSvnRepo1Provider svnRepo1UrlProvider;
  @Inject private TestSvnUsernameProvider svnUsernameProvider;
  @Inject private TestSvnPasswordProvider svnPasswordProvider;

  @Inject private Menu menu;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Wizard wizard;
  @Inject private ImportProjectFromLocation importProjectFromLocation;
  @Inject private Loader loader;
  @Inject private Subversion subversion;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private CodenvyEditor editor;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(ws);
  }

  @Test
  public void revertTest() throws Exception {
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
    loader.waitOnClosed();
    projectExplorer.scrollToItemByPath(PROJECT_NAME + "/README.txt");
    projectExplorer.openItemByPath(REVERT_FILE);
    editor.typeTextIntoEditor("changes", 1);
    menu.runAndWaitCommand(SUBVERSION, SVN_STATUS);
    loader.waitOnClosed();
    projectExplorer.selectItem(REVERT_FILE);
    menu.runAndWaitCommand(SUBVERSION, SVN_REVERT);
    subversion.waitSvnRevertFormOpened();
    subversion.clickSvnRevertOk();
    subversion.waitSvnRevertFormClosed();
    menu.runAndWaitCommand(SUBVERSION, SVN_STATUS);
    loader.waitOnClosed();
    projectExplorer.selectItem(REVERT_FOLDER);
    menu.runCommand(PROJECT, NEW, FILE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText("tmp.txt");
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    projectExplorer.selectItem(REVERT_FOLDER + "/tmp.txt");
    menu.runAndWaitCommand(SUBVERSION, SVN_ADD);
    loader.waitOnClosed();
    projectExplorer.selectItem(REVERT_FILE);
    menu.runCommand(SUBVERSION, SVN_DELETE);
    loader.waitOnClosed();
    projectExplorer.selectItem(REVERT_FOLDER);
    menu.runAndWaitCommand(SUBVERSION, SVN_REVERT);
    subversion.waitSvnRevertFormOpened();
    subversion.clickSvnRevertOk();
    subversion.waitSvnRevertFormClosed();
    menu.runAndWaitCommand(SUBVERSION, SVN_STATUS);
    loader.waitOnClosed();
  }
}
