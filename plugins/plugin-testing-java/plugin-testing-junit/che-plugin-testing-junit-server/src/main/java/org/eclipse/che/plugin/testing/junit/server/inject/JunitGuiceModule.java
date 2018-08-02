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
