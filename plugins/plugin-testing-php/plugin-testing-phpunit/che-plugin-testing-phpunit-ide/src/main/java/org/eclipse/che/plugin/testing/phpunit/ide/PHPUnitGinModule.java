/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.phpunit.ide;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.plugin.testing.ide.action.TestAction;
import org.eclipse.che.plugin.testing.ide.detector.TestFileExtension;
import org.eclipse.che.plugin.testing.phpunit.ide.action.PHPUnitTestActionGroup;

/**
 * PHPUnit Gin module.
 *
 * @author Bartlomiej Laczkowski
 */
@ExtensionGinModule
public class PHPUnitGinModule extends AbstractGinModule {
  @Override
  protected void configure() {
    GinMultibinder.newSetBinder(binder(), TestAction.class)
        .addBinding()
        .to(PHPUnitTestActionGroup.class);

    GinMultibinder.newSetBinder(binder(), TestFileExtension.class)
        .addBinding()
        .to(PHPTestFileExtension.class);
  }
}
