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
