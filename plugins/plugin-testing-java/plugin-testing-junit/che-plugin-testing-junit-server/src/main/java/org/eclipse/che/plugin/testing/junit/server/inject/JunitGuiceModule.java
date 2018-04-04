/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.junit.server.inject;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

import com.google.inject.AbstractModule;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.testing.junit.server.junit4.JUnit4TestRunner;

/** @author Mirage Abeysekara */
@DynaModule
public class JunitGuiceModule extends AbstractModule {
  @Override
  protected void configure() {
    newSetBinder(binder(), org.eclipse.che.api.testing.server.framework.TestRunner.class)
        .addBinding()
        .to(JUnit4TestRunner.class);
  }
}
