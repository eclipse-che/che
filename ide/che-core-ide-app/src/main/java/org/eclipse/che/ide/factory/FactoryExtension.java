/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.factory;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_WORKSPACE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.factory.accept.AcceptFactoryHandler;
import org.eclipse.che.ide.factory.action.CreateFactoryAction;
import org.eclipse.che.ide.factory.welcome.OpenWelcomePageAction;

/** @author Vladyslav Zhukovskii */
@Singleton
@Extension(title = "Factory", version = "3.0.0")
public class FactoryExtension {

  @Inject
  public FactoryExtension(
      AcceptFactoryHandler acceptFactoryHandler,
      ActionManager actionManager,
      FactoryResources resources,
      CreateFactoryAction configureFactoryAction,
      OpenWelcomePageAction openWelcomePageAction) {
    acceptFactoryHandler.process();

    resources.factoryCSS().ensureInjected();

    DefaultActionGroup workspaceGroup =
        (DefaultActionGroup) actionManager.getAction(GROUP_WORKSPACE);

    actionManager.registerAction("openWelcomePage", openWelcomePageAction);
    actionManager.registerAction("configureFactoryAction", configureFactoryAction);

    workspaceGroup.add(configureFactoryAction);
  }
}
