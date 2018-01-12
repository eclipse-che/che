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
package org.eclipse.che.plugin.debugger.ide.debug.breakpoint;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;

/**
 * Groups actions that can be applied upon a breakpoint.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class BreakpointActionGroup extends DefaultActionGroup {

  @Inject
  public BreakpointActionGroup(ActionManager actionManager) {
    super("breakpointConfiguration", false, actionManager);
  }
}
