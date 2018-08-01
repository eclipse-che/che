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
package org.eclipse.che.plugin.debugger.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.eclipse.che.plugin.debugger.ide.configuration.DebugConfigurationAction;
import org.eclipse.che.plugin.debugger.ide.configuration.DebugConfigurationActionFactory;
import org.eclipse.che.plugin.debugger.ide.configuration.DebugConfigurationsManagerImpl;
import org.eclipse.che.plugin.debugger.ide.configuration.EditDebugConfigurationsView;
import org.eclipse.che.plugin.debugger.ide.configuration.EditDebugConfigurationsViewImpl;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerToolbar;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerView;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerViewImpl;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerWatchToolBar;
import org.eclipse.che.plugin.debugger.ide.debug.breakpoint.BreakpointConfigurationView;
import org.eclipse.che.plugin.debugger.ide.debug.breakpoint.BreakpointConfigurationViewImpl;
import org.eclipse.che.plugin.debugger.ide.debug.breakpoint.BreakpointContextMenuFactory;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.DebuggerDialogFactory;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.common.TextAreaDialogView;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.common.TextAreaDialogViewImpl;
import org.eclipse.che.plugin.debugger.ide.debug.expression.EvaluateExpressionView;
import org.eclipse.che.plugin.debugger.ide.debug.expression.EvaluateExpressionViewImpl;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.DebuggerNodeFactory;

/**
 * GIN module for Debugger extension.
 *
 * @author Andrey Plotnikov
 * @author Artem Zatsarynnyi
 * @author Oleksandr Andriienko
 */
@ExtensionGinModule
public class DebuggerGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(DebuggerView.class).to(DebuggerViewImpl.class).in(Singleton.class);
    bind(EvaluateExpressionView.class).to(EvaluateExpressionViewImpl.class).in(Singleton.class);
    bind(BreakpointConfigurationView.class).to(BreakpointConfigurationViewImpl.class);
    bind(EditDebugConfigurationsView.class)
        .to(EditDebugConfigurationsViewImpl.class)
        .in(Singleton.class);

    bind(DebugConfigurationsManager.class)
        .to(DebugConfigurationsManagerImpl.class)
        .in(Singleton.class);
    install(
        new GinFactoryModuleBuilder()
            .implement(BaseAction.class, DebugConfigurationAction.class)
            .build(DebugConfigurationActionFactory.class));
    install(
        new GinFactoryModuleBuilder()
            .implement(TextAreaDialogView.class, TextAreaDialogViewImpl.class)
            .build(DebuggerDialogFactory.class));
    install(new GinFactoryModuleBuilder().build(DebuggerNodeFactory.class));

    bind(ToolbarPresenter.class)
        .annotatedWith(DebuggerWatchToolBar.class)
        .to(ToolbarPresenter.class)
        .in(Singleton.class);

    bind(ToolbarPresenter.class)
        .annotatedWith(DebuggerToolbar.class)
        .to(ToolbarPresenter.class)
        .in(Singleton.class);

    install(new GinFactoryModuleBuilder().build(BreakpointContextMenuFactory.class));
  }
}
