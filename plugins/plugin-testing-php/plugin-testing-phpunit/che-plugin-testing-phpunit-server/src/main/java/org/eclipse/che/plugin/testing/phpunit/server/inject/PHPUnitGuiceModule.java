/**
 * ***************************************************************************** Copyright (c) 2016
 * Rogue Wave Software, Inc. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Rogue Wave Software, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.testing.phpunit.server.inject;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.testing.server.framework.TestRunner;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.testing.phpunit.server.PHPUnitTestRunner;

/**
 * PHPUnit Guice module.
 *
 * @author Bartlomiej Laczkowski
 */
@DynaModule
public class PHPUnitGuiceModule extends AbstractModule {
  @Override
  protected void configure() {
    newSetBinder(binder(), TestRunner.class).addBinding().to(PHPUnitTestRunner.class);
  }
}
