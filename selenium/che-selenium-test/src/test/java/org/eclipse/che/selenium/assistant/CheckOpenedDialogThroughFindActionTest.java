/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.assistant;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.DialogAbout;
import org.eclipse.che.selenium.pageobject.FindAction;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckOpenedDialogThroughFindActionTest {

  @Inject private TestWorkspace testWorkspace;
  @Inject private FindAction findAction;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Ide ide;
  @Inject private DialogAbout dialogAbout;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
  }

  @Test
  public void findActionAndRunItTest() {
    projectExplorer.waitProjectExplorer();
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.FIND_ACTION);
    findAction.clearTextBoxActionForm();
    findAction.typeTextIntoFindActionForm("Abo");
    findAction.waitTextInFormFindAction("About  Help");
    findAction.clickOnFoundAction("About");
    dialogAbout.waitAboutDialogIsOpen();
    dialogAbout.closeAboutDialog();
  }
}
