/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.php.zdb.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.fqn.FqnResolverFactory;

/**
 * Extension allows debug PHP applications with the use of Zend Debugger.
 *
 * @author Bartlomiej Laczkowski
 */
@Singleton
@Extension(title = "Zend Debugger", version = "1.0.0")
public class ZendDebuggerExtension {

    @Inject
    public ZendDebuggerExtension(DebuggerManager debuggerManager,
                                 ZendDebugger zendDebugger,
                                 FqnResolverFactory resolverFactory,
                                 ZendDbgFqnResolver zendFqnResolver) {
        debuggerManager.registeredDebugger(ZendDebugger.ID, zendDebugger);
        resolverFactory.addResolver("php", zendFqnResolver);
        resolverFactory.addResolver("phtml", zendFqnResolver);
    }
    
}
