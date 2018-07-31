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
package org.eclipse.che.selenium.miscellaneous;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Help.ABOUT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Help.HELP;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.DialogAbout;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.Test;

/** @author Anatolii Bazko */
public class DialogAboutTest {

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private DialogAbout dialogAbout;

  @Test
  public void dialogAboutTest() throws Exception {
    ide.open(testWorkspace);

    ide.waitOpenedWorkspaceIsReadyToUse();
    menu.runCommand(HELP, ABOUT);

    dialogAbout.waitVerifyTextElements("About");
    dialogAbout.waitVerifyTextElements("Build Details");
    dialogAbout.clickOnBuildDetailsAnchor();
    dialogAbout.waitBuildDetailsDialogIsOpen();
    dialogAbout.closeBuildDetailsDialog();
    dialogAbout.closeAboutDialog();
  }
}
