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
package org.eclipse.che.plugin.testing.testng.server.inject;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.testing.server.framework.TestRunner;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.testing.testng.server.TestNGRunner;

/** @author Mirage Abeysekara */
@DynaModule
public class TestNGGuiceModule extends AbstractModule {
  @Override
  protected void configure() {
    newSetBinder(binder(), TestRunner.class).addBinding().to(TestNGRunner.class);
  }
}
