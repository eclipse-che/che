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
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SVN_GET_LOCK;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SVN_RELEASE_LOCK;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.IMPORT_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.provider.TestSvnPasswordProvider;
import org.eclipse.che.selenium.core.provider.TestSvnRepo1Provider;
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
import org.eclipse.che.selenium.pageobject.subversion.SvnCommit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Anton Korneta */
public class LockFileTest {

  private static final String PROJECT_NAME = NameGenerator.generate("LockFileTestProject", 6);
  private static final String LOCKED_FILE_PATH = PROJECT_NAME + "/tags/lock-test/lock-file1.txt";
  private static final String WARNING_MESSAGE = "svn: warning: W160035:";

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
  @Inject private SvnCommit svnCommit;
  @Inject private CodenvyEditor editor;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(ws);
  }

  @Test
  public void lockFileTest() throws Exception {
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
    projectExplorer.openItemByPath(PROJECT_NAME);

    projectExplorer.openItemByPath(PROJECT_NAME + "/tags");
    projectExplorer.openItemByPath(PROJECT_NAME + "/tags/lock-test");
    projectExplorer.selectItem(LOCKED_FILE_PATH);
    menu.runCommand(SUBVERSION, SVN_GET_LOCK);
    subversion.waitSvnGetLockFormOpened();
    subversion.clickSvnGetLock();
    subversion.waitSvnStatusBarInfoPanelOpened();
    if (!subversion.getAllMessageFromSvnStatusBar().contains(WARNING_MESSAGE)) {
      projectExplorer.openItemByPath(LOCKED_FILE_PATH);
      editor.typeTextIntoEditor("changes for commit");
      projectExplorer.openItemByPath(LOCKED_FILE_PATH);
      menu.runCommand(SUBVERSION, SVN_COMMIT);
      svnCommit.waitMainFormOpened();
      svnCommit.typeCommitMess("message");
      svnCommit.clickCommitBtn();
      projectExplorer.selectItem(LOCKED_FILE_PATH);
      menu.runAndWaitCommand(SUBVERSION, SVN_RELEASE_LOCK);
      subversion.waitSvnReleaseLockFormOpened();
      subversion.clickSvnReleaseLockUnlock();
      subversion.waitSvnResolveFormClosed();
    } else {
      menu.runAndWaitCommand(SUBVERSION, SVN_RELEASE_LOCK);
      subversion.waitSvnReleaseLockFormOpened();
      subversion.clickSvnReleaseLockForceUnlock();
      subversion.waitSvnResolveFormClosed();
    }
  }
}
