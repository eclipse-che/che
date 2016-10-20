/*******************************************************************************
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend Technologies - initial API and implementation
 *******************************************************************************/
package zend.com.che.plugin.zdb.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import zend.com.che.plugin.zdb.server.utils.ZendDbgUtils;

import org.eclipse.che.api.debugger.server.DebuggerFactory;
import org.eclipse.che.inject.DynaModule;

/**
 * @author Bartlomiej Laczkowski
 */
@DynaModule
public class ZendDebuggerModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), DebuggerFactory.class).addBinding().to(ZendDebuggerFactory.class);
        bind(ZendDbgUtils.class);
    }
    
}
