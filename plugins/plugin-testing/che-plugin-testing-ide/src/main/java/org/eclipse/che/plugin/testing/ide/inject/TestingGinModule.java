/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.testing.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.plugin.testing.ide.view.TestResultView;
import org.eclipse.che.plugin.testing.ide.view.TestResultViewImpl;
import org.eclipse.che.plugin.testing.ide.view.navigation.factory.TestResultNodeFactory;
/**
 * Gin Module for test runner extension.
 *
 * @author Mirage Abeysekara
 */
@ExtensionGinModule
public class TestingGinModule extends AbstractGinModule{
    @Override
    protected void configure() {
        install(new GinFactoryModuleBuilder().build(TestResultNodeFactory.class));
        bind(TestResultView.class).to(TestResultViewImpl.class);
    }
}
