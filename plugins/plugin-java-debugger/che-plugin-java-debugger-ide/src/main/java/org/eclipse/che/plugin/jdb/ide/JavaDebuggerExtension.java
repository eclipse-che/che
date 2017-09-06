/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.jdb.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerResourceHandlerFactory;
import org.eclipse.che.plugin.jdb.ide.debug.JavaDebugger;
import org.eclipse.che.plugin.jdb.ide.debug.JavaDebuggerResourceHandler;

/**
 * Extension allows debug Java web applications.
 *
 * @author Andrey Plotnikov
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 * @author Anatoliy Bazko
 * @author Mykola Morhun
 */
@Singleton
@Extension(title = "Java Debugger", version = "4.1.0")
public class JavaDebuggerExtension {

  @Inject
  public JavaDebuggerExtension(
      DebuggerManager debuggerManager,
      JavaDebugger javaDebugger,
      DebuggerResourceHandlerFactory debuggerResourceHandlerFactory,
      JavaDebuggerResourceHandler javaDebuggerResourceHandler) {
    debuggerManager.registeredDebugger(JavaDebugger.ID, javaDebugger);
    debuggerResourceHandlerFactory.register(JavaDebugger.ID, javaDebuggerResourceHandler);
  }
}
