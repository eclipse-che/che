/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.refactor;

import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;

/**
 * //
 *
 * @author Musienko Maxim
 */
public class Services {
  private final ProjectExplorer projectExplorer;
  private final NotificationsPopupPanel notificationsPopupPanel;
  private final Refactor refactor;

  public Services(
      ProjectExplorer projectExplorer,
      NotificationsPopupPanel notificationsPopupPanel,
      Refactor refactor) {

    this.projectExplorer = projectExplorer;
    this.notificationsPopupPanel = notificationsPopupPanel;
    this.refactor = refactor;
  }

  public void expandRenameTypesProject(String nameOfProject) {
    expandToJavaPath(nameOfProject);
    projectExplorer.waitItem(nameOfProject + "/src" + "/main" + "/java" + "/renametype");
    projectExplorer.openItemByPath(nameOfProject + "/src" + "/main" + "/java" + "/renametype");
  }

  /** @param nameOfProject name of project to expand */
  private void expandToJavaPath(String nameOfProject) {
    projectExplorer.openItemByPath(nameOfProject);
    projectExplorer.waitItem(nameOfProject + "/src");
    projectExplorer.openItemByPath(nameOfProject + "/src");
    projectExplorer.waitItem(nameOfProject + "/src" + "/main");
    projectExplorer.openItemByPath(nameOfProject + "/src" + "/main");
    projectExplorer.waitItem(nameOfProject + "/src" + "/main" + "/java");
    projectExplorer.openItemByPath(nameOfProject + "/src" + "/main" + "/java");
  }

  /**
   * @param nameOfProject
   * @param rootPackage
   */
  public void expandRenamePrivateMethodProject(String nameOfProject, String rootPackage) {
    expandToJavaPath(nameOfProject);
    projectExplorer.waitItem(nameOfProject + "/src" + "/main" + "/java" + "/" + rootPackage);
    projectExplorer.openItemByPath(nameOfProject + "/src" + "/main" + "/java" + "/" + rootPackage);
  }

  /** @param projectName */
  public void expandSpringProjectNodes(String projectName) {
    projectExplorer.openItemByPath(projectName);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    projectExplorer.openItemByPath(projectName + "/src");
    projectExplorer.openItemByPath(projectName + "/src" + "/main");
    projectExplorer.openItemByPath(projectName + "/src" + "/main" + "/java");
  }

  public void invokeRefactorWizardForProjectExplorerItem(String pathThToItem) {
    projectExplorer.waitAndSelectItem(pathThToItem);
    projectExplorer.launchRefactorByKeyboard();
    refactor.waitRenameCompilationUnitFormIsOpen();
  }
}
