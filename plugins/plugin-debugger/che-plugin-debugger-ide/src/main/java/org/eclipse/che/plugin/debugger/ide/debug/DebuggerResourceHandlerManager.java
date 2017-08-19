/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Set;

/**
 * @author Anatolii Bazko
 */
@Singleton
public class DebuggerResourceHandlerManager {

    @Inject
    public DebuggerResourceHandlerManager(Set<DebuggerResourceHandler> handlers) {
    }

    public DebuggerResourceHandler get(String debuggerType) {
        return null;
    }

    // TODO bind
}
