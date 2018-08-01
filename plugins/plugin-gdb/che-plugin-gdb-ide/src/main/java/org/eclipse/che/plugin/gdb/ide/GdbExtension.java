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
package org.eclipse.che.plugin.gdb.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.debug.DebuggerManager;

/**
 * Extension allows to debug CPP applications.
 *
 * @author Anatoliy Bazko
 */
@Singleton
@Extension(title = "GDB", version = "4.0.0")
public class GdbExtension {

  @Inject
  public GdbExtension(DebuggerManager debuggerManager, GdbDebugger gdbDebugger) {
    debuggerManager.registeredDebugger(GdbDebugger.ID, gdbDebugger);
  }
}
