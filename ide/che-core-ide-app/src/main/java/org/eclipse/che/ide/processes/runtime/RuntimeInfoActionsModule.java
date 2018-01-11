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
package org.eclipse.che.ide.processes.runtime;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_CONSOLES_TREE_CONTEXT_MENU;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;

/**
 * Performs common operations e.g. registering action, etc.
 *
 * @author Vlad Zhukovskyi
 * @since 5.18.0
 */
@Singleton
public class RuntimeInfoActionsModule {

  @Inject
  public RuntimeInfoActionsModule(
      ActionManager actionManager, ShowRuntimeInfoAction showRuntimeInfoAction) {
    actionManager.registerAction(ShowRuntimeInfoAction.ID, showRuntimeInfoAction);

    DefaultActionGroup consolesTreeContextMenu =
        (DefaultActionGroup) actionManager.getAction(GROUP_CONSOLES_TREE_CONTEXT_MENU);
    consolesTreeContextMenu.add(showRuntimeInfoAction);
  }
}
