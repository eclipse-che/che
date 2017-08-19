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
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SVN_COPY;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Subversion.SVN_STATUS;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.IMPORT_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;

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
public class CopyTest {

  private static final String PROJECT_NAME = NameGenerator.generate("CopyTestProject", 6);
  private static final String FROM_PATH = PROJECT_NAME + "/trunk/copy/copy-from";
  private static final String TO_PATH = PROJECT_NAME + "/trunk/copy/copy-to";
  private static final String COPY_FOLDER = "copy-folder";
  private static final String COPY_FILE = "copy-file.txt";
  private static final Logger LOG = LoggerFactory.getLogger(CopyTest.class);

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
  public void svnCopyTest() throws Exception {
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
    copy(TO_PATH, FROM_PATH + "/" + COPY_FILE);
    loader.waitOnClosed();
    copy(TO_PATH, FROM_PATH + "/" + COPY_FOLDER);
    loader.waitOnClosed();
    menu.runCommand(SUBVERSION, SVN_STATUS);
  }

  private void copy(String targetPath, String itemPath) throws Exception {
    projectExplorer.selectItem(itemPath);
    menu.runAndWaitCommand(SUBVERSION, SVN_COPY);
    subversion.waitSvnCopyFormOpened();
    subversion.openSvnCopyItemByPath(PROJECT_NAME + "/trunk");
    subversion.openSvnCopyItemByPath(PROJECT_NAME + "/trunk/copy");
    subversion.openSvnCopyItemByPath(targetPath);
    subversion.clickSvnCopyCopyButton();
    subversion.waitSvnCopyFormClosed();
  }
}
