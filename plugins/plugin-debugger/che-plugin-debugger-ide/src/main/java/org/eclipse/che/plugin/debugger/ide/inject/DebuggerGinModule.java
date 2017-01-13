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
package org.eclipse.che.plugin.debugger.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.plugin.debugger.ide.configuration.DebugConfigurationAction;
import org.eclipse.che.plugin.debugger.ide.configuration.DebugConfigurationActionFactory;
import org.eclipse.che.plugin.debugger.ide.configuration.DebugConfigurationsManagerImpl;
import org.eclipse.che.plugin.debugger.ide.configuration.EditDebugConfigurationsView;
import org.eclipse.che.plugin.debugger.ide.configuration.EditDebugConfigurationsViewImpl;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerToolbar;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerView;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerViewImpl;
import org.eclipse.che.plugin.debugger.ide.debug.changevalue.ChangeValueView;
import org.eclipse.che.plugin.debugger.ide.debug.changevalue.ChangeValueViewImpl;
import org.eclipse.che.plugin.debugger.ide.debug.expression.EvaluateExpressionView;
import org.eclipse.che.plugin.debugger.ide.debug.expression.EvaluateExpressionViewImpl;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.eclipse.che.ide.util.storage.BrowserLocalStorageProviderImpl;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;

/**
 * GIN module for Debugger extension.
 *
 * @author Andrey Plotnikov
 * @author Artem Zatsarynnyi
 */
@ExtensionGinModule
public class DebuggerGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(DebuggerView.class).to(DebuggerViewImpl.class).in(Singleton.class);
        bind(EvaluateExpressionView.class).to(EvaluateExpressionViewImpl.class).in(Singleton.class);
        bind(ChangeValueView.class).to(ChangeValueViewImpl.class).in(Singleton.class);
        bind(EditDebugConfigurationsView.class).to(EditDebugConfigurationsViewImpl.class).in(Singleton.class);

        bind(DebugConfigurationsManager.class).to(DebugConfigurationsManagerImpl.class).in(Singleton.class);
        install(new GinFactoryModuleBuilder().implement(Action.class, DebugConfigurationAction.class)
                                             .build(DebugConfigurationActionFactory.class));

        bind(LocalStorageProvider.class).to(BrowserLocalStorageProviderImpl.class).in(Singleton.class);
        bind(ToolbarPresenter.class).annotatedWith(DebuggerToolbar.class).to(ToolbarPresenter.class).in(Singleton.class);
    }
}
