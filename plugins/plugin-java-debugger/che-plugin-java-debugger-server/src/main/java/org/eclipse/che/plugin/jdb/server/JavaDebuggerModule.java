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
package org.eclipse.che.plugin.jdb.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.debugger.server.DebuggerFactory;
import org.eclipse.che.inject.DynaModule;

/** @author Anatoliy Bazko */
@DynaModule
public class JavaDebuggerModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), DebuggerFactory.class)
        .addBinding()
        .to(JavaDebuggerFactory.class);
  }
}
