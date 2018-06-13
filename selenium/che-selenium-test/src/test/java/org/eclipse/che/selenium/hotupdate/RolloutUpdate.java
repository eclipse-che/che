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
package org.eclipse.che.selenium.hotupdate;

import javax.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.provider.AdminTestUserProvider;
import org.eclipse.che.selenium.core.requestfactory.CheTestDefaultHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.requestfactory.TestHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.testng.annotations.Test;

public class RolloutUpdate {
  private static final String          NAME_OF_ARTIFACT = NameGenerator.generate("quickStart", 4);
  @Inject private      Wizard          projectWizard;
  @Inject private      Menu            menu;
  @Inject private      ProjectExplorer projectExplorer;
  @Inject private      Consoles        console;
  @Inject private      CodenvyEditor   editor;
  @Inject private      Ide             ide;
  @Inject private      TestWorkspace   workspace;

  @Test
  public void createMavenArchetypeStartProjectByWizard() throws Exception {
    ide.open(workspace);
  }
}
