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
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SVN_RESOLVE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SVN_UPDATE_TO_REVISION;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Anton Korneta
 * @author Andrey Chizhikov
 */
public class ResolveTest {

  private static final String PROJECT_NAME = NameGenerator.generate("ResolveTestProject", 6);
  private static final String CONFLICT_FILE = PROJECT_NAME + "/tags/file.txt";
  private static final String CONFLICT_CHANGES = " Hello World ";
  private static final Logger LOG = LoggerFactory.getLogger(ResolveTest.class);

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
  @Inject private CodenvyEditor editor;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(ws);
  }

  @Test
  public void resolveTest() throws Exception {
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
    projectExplorer.openItemByPath(CONFLICT_FILE);
    editor.typeTextIntoEditor(CONFLICT_CHANGES, 1);
    menu.runCommand(SUBVERSION, SVN_UPDATE_TO_REVISION);
    subversion.waitSvnUpdateToRevisionFormOpened();
    subversion.selectSvnUpdateToRevision();
    subversion.typeSvnUpdateToRevisionName("2");

    subversion.clickSvnUpdateToRevisionButtonUpdate();
    subversion.waitSvnUpdateToRevisionFormClosed();
    resolve(CONFLICT_FILE, "mine-conflict");
    loader.waitOnClosed();
    menu.runCommand(SUBVERSION, SVN_UPDATE_TO_REVISION);
    subversion.waitSvnUpdateToRevisionFormOpened();
    subversion.selectSvnUpdateToRevision();
    subversion.typeSvnUpdateToRevisionName("3");
    subversion.clickSvnUpdateToRevisionButtonUpdate();
    subversion.waitSvnUpdateToRevisionFormClosed();
    resolve(CONFLICT_FILE, "theirs-conflict");
  }

  private void resolve(String conflictFilePath, String resolveType) throws Exception {
    projectExplorer.selectItem(conflictFilePath);
    projectExplorer.waitItem(conflictFilePath);
    menu.runCommand(SUBVERSION, SVN_RESOLVE);
    subversion.waitSvnResolveFormOpened();
    subversion.selectSvnResolveType(resolveType);
    subversion.clickSvnResolveConfirm();
    subversion.waitSvnResolveFormClosed();
  }
}
