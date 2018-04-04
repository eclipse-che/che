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
