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
package org.eclipse.che.plugin.jdb.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerLocationHandlerManager;
import org.eclipse.che.plugin.jdb.ide.debug.ExternalResourceLocationHandler;
import org.eclipse.che.plugin.jdb.ide.debug.JavaDebugger;

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
      DebuggerLocationHandlerManager debuggerLocationHandlerManager,
      ExternalResourceLocationHandler javaDebuggerResourceHandler) {
    debuggerManager.registeredDebugger(JavaDebugger.ID, javaDebugger);
    debuggerLocationHandlerManager.register(javaDebuggerResourceHandler);
  }
}
