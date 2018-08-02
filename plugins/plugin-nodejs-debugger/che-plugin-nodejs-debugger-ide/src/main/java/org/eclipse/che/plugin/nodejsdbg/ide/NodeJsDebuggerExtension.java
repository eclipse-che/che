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
package org.eclipse.che.plugin.nodejsdbg.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.debug.DebuggerManager;

/**
 * Extension allows to debug NodeJs applications.
 *
 * @author Anatoliy Bazko
 */
@Singleton
@Extension(title = "NodeJs Debugger", version = "5.0.0")
public class NodeJsDebuggerExtension {

  @Inject
  public NodeJsDebuggerExtension(DebuggerManager debuggerManager, NodeJsDebugger nodeJsDebugger) {
    debuggerManager.registeredDebugger(NodeJsDebugger.ID, nodeJsDebugger);
  }
}
