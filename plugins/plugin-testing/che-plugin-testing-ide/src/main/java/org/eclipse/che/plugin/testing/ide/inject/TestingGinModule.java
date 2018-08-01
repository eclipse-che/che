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
package org.eclipse.che.plugin.testing.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;

/**
 * Gin Module for test runner extension.
 *
 * @author Mirage Abeysekara
 */
@ExtensionGinModule
public class TestingGinModule extends AbstractGinModule {
  @Override
  protected void configure() {
    install(
        new GinFactoryModuleBuilder()
            .build(
                org.eclipse.che.plugin.testing.ide.view.navigation.factory.TestResultNodeFactory
                    .class));

    bind(org.eclipse.che.plugin.testing.ide.view.TestResultView.class)
        .to(org.eclipse.che.plugin.testing.ide.view.TestResultViewImpl.class);
  }
}
