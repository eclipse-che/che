/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import org.eclipse.che.api.machine.gwt.client.MachineManager;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.core.Component;
import org.eclipse.che.ide.extension.machine.client.MachineComponent;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;
import org.eclipse.che.ide.extension.machine.client.command.custom.CustomCommandType;
import org.eclipse.che.ide.extension.machine.client.command.edit.EditCommandsView;
import org.eclipse.che.ide.extension.machine.client.command.edit.EditCommandsViewImpl;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CommandPropertyValueProvider;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CommandPropertyValueProviderRegistry;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CommandPropertyValueProviderRegistryImpl;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CurrentProjectPathProvider;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CurrentProjectRelativePathProvider;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.DevMachineHostNameProvider;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.inject.factories.TerminalFactory;
import org.eclipse.che.ide.extension.machine.client.inject.factories.WidgetsFactory;
import org.eclipse.che.ide.extension.machine.client.machine.MachineManagerImpl;
import org.eclipse.che.ide.extension.machine.client.machine.console.MachineConsoleToolbar;
import org.eclipse.che.ide.extension.machine.client.machine.console.MachineConsoleView;
import org.eclipse.che.ide.extension.machine.client.machine.console.MachineConsoleViewImpl;
import org.eclipse.che.ide.extension.machine.client.machine.create.CreateMachineView;
import org.eclipse.che.ide.extension.machine.client.machine.create.CreateMachineViewImpl;
import org.eclipse.che.ide.extension.machine.client.outputspanel.OutputsContainerView;
import org.eclipse.che.ide.extension.machine.client.outputspanel.OutputsContainerViewImpl;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsole;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsolePresenter;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.OutputConsoleViewImpl;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.DefaultOutputConsole;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.OutputConsoleView;
import org.eclipse.che.ide.extension.machine.client.perspective.MachinePerspective;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.button.EditorButtonWidget;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.button.EditorButtonWidgetImpl;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.Tab;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.TabImpl;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header.TabHeader;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header.TabHeaderImpl;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelView;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelViewImpl;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;


import static org.eclipse.che.ide.extension.machine.client.perspective.MachinePerspective.MACHINE_PERSPECTIVE_ID;

/**
 * GIN module for Machine extension.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@ExtensionGinModule
public class MachineGinModule extends AbstractGinModule {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        GinMapBinder<String, Component> componentBinder = GinMapBinder.newMapBinder(binder(), String.class, Component.class);
        componentBinder.addBinding("Start Machine").to(MachineComponent.class);

        GinMapBinder<String, Perspective> perspectiveBinder = GinMapBinder.newMapBinder(binder(), String.class, Perspective.class);
        perspectiveBinder.addBinding(MACHINE_PERSPECTIVE_ID).to(MachinePerspective.class);

        bind(ToolbarPresenter.class).annotatedWith(MachineConsoleToolbar.class).to(ToolbarPresenter.class).in(Singleton.class);
        bind(MachineConsoleView.class).to(MachineConsoleViewImpl.class).in(Singleton.class);

        bind(CreateMachineView.class).to(CreateMachineViewImpl.class);
        bind(OutputConsoleView.class).to(OutputConsoleViewImpl.class);
        install(new GinFactoryModuleBuilder().implement(CommandOutputConsole.class, Names.named("command"), CommandOutputConsolePresenter.class)
                                             .implement(OutputConsole.class, Names.named("default"), DefaultOutputConsole.class)
                                             .build(CommandConsoleFactory.class));

        bind(OutputsContainerView.class).to(OutputsContainerViewImpl.class).in(Singleton.class);
        bind(ConsolesPanelView.class).to(ConsolesPanelViewImpl.class).in(Singleton.class);

        bind(EditCommandsView.class).to(EditCommandsViewImpl.class).in(Singleton.class);

        GinMultibinder.newSetBinder(binder(), CommandType.class).addBinding().to(CustomCommandType.class);

        bind(CommandPropertyValueProviderRegistry.class).to(CommandPropertyValueProviderRegistryImpl.class).in(Singleton.class);

        final GinMultibinder<CommandPropertyValueProvider> valueProviderBinder = GinMultibinder.newSetBinder(binder(), CommandPropertyValueProvider.class);
        valueProviderBinder.addBinding().to(DevMachineHostNameProvider.class);
        valueProviderBinder.addBinding().to(CurrentProjectPathProvider.class);
        valueProviderBinder.addBinding().to(CurrentProjectRelativePathProvider.class);

        install(new GinFactoryModuleBuilder().implement(TabHeader.class, TabHeaderImpl.class)
                                             .implement(EditorButtonWidget.class, EditorButtonWidgetImpl.class)
                                             .build(WidgetsFactory.class));
        install(new GinFactoryModuleBuilder().implement(Tab.class, TabImpl.class).build(EntityFactory.class));
        install(new GinFactoryModuleBuilder().build(TerminalFactory.class));

        bind(MachineManager.class).to(MachineManagerImpl.class).in(Singleton.class);
    }
}
