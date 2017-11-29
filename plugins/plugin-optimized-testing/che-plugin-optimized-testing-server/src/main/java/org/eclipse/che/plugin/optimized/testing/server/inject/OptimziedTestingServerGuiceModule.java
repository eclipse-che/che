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
package org.eclipse.che.plugin.optimized.testing.server.inject;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

import com.google.inject.AbstractModule;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.java.testing.JavaTestFinder;
import org.eclipse.che.plugin.optimized.testing.server.OptimizedJavaTestFinder;
import org.eclipse.che.plugin.optimized.testing.server.OptimizedTestingRPCService;

@DynaModule
public class OptimziedTestingServerGuiceModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(OptimizedTestingRPCService.class);
    newSetBinder(binder(), JavaTestFinder.class).addBinding().to(OptimizedJavaTestFinder.class);
  }
}
