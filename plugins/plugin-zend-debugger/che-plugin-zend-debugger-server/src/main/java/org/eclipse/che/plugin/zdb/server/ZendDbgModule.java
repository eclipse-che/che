/**
 * ***************************************************************************** Copyright (c) 2016
 * Rogue Wave Software, Inc. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Rogue Wave Software, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.zdb.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.debugger.server.DebuggerFactory;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.zdb.server.utils.ZendDbgFileUtils;

/**
 * Zend debugger GIN module.
 *
 * @author Bartlomiej Laczkowski
 */
@DynaModule
public class ZendDbgModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), DebuggerFactory.class).addBinding().to(ZendDbgFactory.class);
    bind(ZendDbgFileUtils.class).asEagerSingleton();
  }
}
