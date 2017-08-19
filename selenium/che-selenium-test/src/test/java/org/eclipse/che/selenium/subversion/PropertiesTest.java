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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.IMPORT_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
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
public class PropertiesTest {

  private static final String PROJECT_NAME = NameGenerator.generate("PropertiesTestProject", 6);
  private static final String FOLDER_PATH = PROJECT_NAME + "/trunk/properties-test";
  private static final String CUSTOM_PROPERTY = "custom";
  private static final String COPYRIGHT_PROPERTY = "copyright";
  private static final String CUSTOM_PROPERTY_VALUE =
      "'custom property for all file in trunk' trunk/*";
  private static final String COPYRIGHT_PROPERTY_VALUE = "(c) 2015 Red-Bean Software";
  private static final Logger LOG = LoggerFactory.getLogger(PropertiesTest.class);

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
  public void propertiesTest() throws Exception {
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

    projectExplorer.openItemByPath(PROJECT_NAME + "/trunk");
    projectExplorer.selectItem(FOLDER_PATH);
    addProperty(CUSTOM_PROPERTY, CUSTOM_PROPERTY_VALUE);
    projectExplorer.selectItem(FOLDER_PATH);
    addProperty(COPYRIGHT_PROPERTY, COPYRIGHT_PROPERTY_VALUE);
    menu.runAndWaitCommand(
        TestMenuCommandsConstants.Subversion.SUBVERSION,
        TestMenuCommandsConstants.Subversion.SVN_STATUS);
    loader.waitOnClosed();
  }

  private void addProperty(String name, String value) throws Exception {
    menu.runAndWaitCommand(
        TestMenuCommandsConstants.Subversion.SUBVERSION,
        TestMenuCommandsConstants.Subversion.SVN_PROPERTIES);
    loader.waitOnClosed();
    subversion.waitSvnPropertiesFormOpened();
    subversion.typeSvnPropertiesPropertyName(name);
    subversion.setSvnPropertiesValue(value);
    subversion.clickSvnApplyProperties();
    subversion.waitSvnPropertiesFormClosed();
  }
}
