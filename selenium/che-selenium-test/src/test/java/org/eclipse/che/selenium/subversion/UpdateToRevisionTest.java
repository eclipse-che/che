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
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SVN_UPDATE_TO_REVISION;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.IMPORT_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.provider.TestSvnPasswordProvider;
import org.eclipse.che.selenium.core.provider.TestSvnRepo1Provider;
import org.eclipse.che.selenium.core.provider.TestSvnUsernameProvider;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
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
public class UpdateToRevisionTest {

  private static final String PROJECT_NAME =
      NameGenerator.generate("UpdateToRevisionTestProject", 2);
  private static final String REVISION = "1";
  private static final String MESSAGE = "At revision 1.";
  private static final Logger LOG = LoggerFactory.getLogger(UpdateToRevisionTest.class);

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

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(ws);
  }

  @Test
  public void updateToRevisionTest() throws Exception {
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

    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);

    menu.runCommand(SUBVERSION, SVN_UPDATE_TO_REVISION);
    loader.waitOnClosed();
    subversion.waitSvnUpdateToRevisionFormOpened();
    subversion.selectSvnUpdateToRevision();
    subversion.typeSvnUpdateToRevisionName(REVISION);
    subversion.clickSvnUpdateToRevisionButtonUpdate();
    loader.waitOnClosed();
    assertTrue(subversion.getAllMessageFromSvnStatusBar().contains(MESSAGE));
  }
}
