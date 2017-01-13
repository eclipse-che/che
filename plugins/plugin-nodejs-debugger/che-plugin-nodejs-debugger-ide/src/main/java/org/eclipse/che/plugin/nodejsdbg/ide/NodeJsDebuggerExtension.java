/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
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
