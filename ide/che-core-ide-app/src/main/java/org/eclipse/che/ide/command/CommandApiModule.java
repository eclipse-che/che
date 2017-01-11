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
package org.eclipse.che.ide.command;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.command.PredefinedCommandGoalRegistry;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.command.editor.CommandEditorView;
import org.eclipse.che.ide.command.editor.CommandEditorViewImpl;
import org.eclipse.che.ide.command.editor.page.text.PageWithTextEditorView;
import org.eclipse.che.ide.command.editor.page.text.PageWithTextEditorViewImpl;
import org.eclipse.che.ide.command.editor.page.settings.SettingsPageView;
import org.eclipse.che.ide.command.editor.page.settings.SettingsPageViewImpl;
import org.eclipse.che.ide.command.execute.GoalPopUpGroupFactory;
import org.eclipse.che.ide.command.execute.ExecuteCommandActionFactory;
import org.eclipse.che.ide.command.execute.ExecuteCommandActionManager;
import org.eclipse.che.ide.command.explorer.CommandsExplorerPresenter;
import org.eclipse.che.ide.command.explorer.CommandsExplorerView;
import org.eclipse.che.ide.command.explorer.CommandsExplorerViewImpl;
import org.eclipse.che.ide.command.goal.BuildGoal;
import org.eclipse.che.ide.command.goal.CommonGoal;
import org.eclipse.che.ide.command.goal.DeployGoal;
import org.eclipse.che.ide.command.goal.PredefinedCommandGoalRegistryImpl;
import org.eclipse.che.ide.command.goal.RunGoal;
import org.eclipse.che.ide.command.goal.TestGoal;
import org.eclipse.che.ide.command.manager.CommandManagerImpl;
import org.eclipse.che.ide.command.node.NodeFactory;
import org.eclipse.che.ide.command.palette.CommandPaletteView;
import org.eclipse.che.ide.command.palette.CommandPaletteViewImpl;
import org.eclipse.che.ide.command.producer.CommandProducerActionFactory;
import org.eclipse.che.ide.command.producer.CommandProducerActionManager;
import org.eclipse.che.ide.command.type.CommandTypeChooserView;
import org.eclipse.che.ide.command.type.CommandTypeChooserViewImpl;
import org.eclipse.che.ide.command.type.CommandTypeRegistryImpl;

import static org.eclipse.che.ide.command.node.CommandFileNode.FILE_TYPE_EXT;

/**
 * GIN module for configuring Command API components.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandApiModule extends AbstractGinModule {

    @Override
    protected void configure() {
        GinMultibinder.newSetBinder(binder(), CommandType.class);

        // predefined goals
        GinMultibinder<CommandGoal> goalBinder = GinMultibinder.newSetBinder(binder(), CommandGoal.class);
        goalBinder.addBinding().to(CommonGoal.class);
        goalBinder.addBinding().to(TestGoal.class);
        goalBinder.addBinding().to(BuildGoal.class);
        goalBinder.addBinding().to(RunGoal.class);
        goalBinder.addBinding().to(DeployGoal.class);

        bind(CommandTypeRegistry.class).to(CommandTypeRegistryImpl.class).in(Singleton.class);
        bind(PredefinedCommandGoalRegistry.class).to(PredefinedCommandGoalRegistryImpl.class).in(Singleton.class);

        bind(CommandManager.class).to(CommandManagerImpl.class).in(Singleton.class);

        GinMapBinder<String, Component> componentBinder = GinMapBinder.newMapBinder(binder(), String.class, Component.class);
        componentBinder.addBinding("CommandManagerImpl").to(CommandManagerImpl.class);
        componentBinder.addBinding("CommandsExplorerPresenter").to(CommandsExplorerPresenter.class);
        componentBinder.addBinding("CommandProducerActionManager").to(CommandProducerActionManager.class);
        componentBinder.addBinding("ExecuteCommandActionManager").to(ExecuteCommandActionManager.class);

        install(new GinFactoryModuleBuilder().build(ExecuteCommandActionFactory.class));
        install(new GinFactoryModuleBuilder().build(GoalPopUpGroupFactory.class));
        install(new GinFactoryModuleBuilder().build(NodeFactory.class));
        install(new GinFactoryModuleBuilder().build(CommandProducerActionFactory.class));

        bind(CommandsExplorerView.class).to(CommandsExplorerViewImpl.class).in(Singleton.class);
        bind(CommandTypeChooserView.class).to(CommandTypeChooserViewImpl.class);
        bind(CommandPaletteView.class).to(CommandPaletteViewImpl.class).in(Singleton.class);

        // command editor
        bind(CommandEditorView.class).to(CommandEditorViewImpl.class);
        bind(SettingsPageView.class).to(SettingsPageViewImpl.class);
        bind(PageWithTextEditorView.class).to(PageWithTextEditorViewImpl.class);
    }

    @Provides
    @Singleton
    @Named("CommandFileType")
    protected FileType provideCommandFileType(Resources resources) {
        return new FileType(resources.defaultImage(), FILE_TYPE_EXT);
    }

    /** Provides the goal which is used for grouping commands which doesn't belong to any goal. */
    @Provides
    @Named("default")
    @Singleton
    protected CommandGoal provideDefaultGoal(CommonGoal commonGoal) {
        return commonGoal;
    }
}
