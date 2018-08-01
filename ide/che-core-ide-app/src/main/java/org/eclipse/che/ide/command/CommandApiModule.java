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
package org.eclipse.che.ide.command;

import static org.eclipse.che.ide.command.node.CommandFileNode.FILE_TYPE_EXT;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandGoalRegistry;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandProducer;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.command.editor.CommandEditorView;
import org.eclipse.che.ide.command.editor.CommandEditorViewImpl;
import org.eclipse.che.ide.command.editor.page.goal.GoalPageView;
import org.eclipse.che.ide.command.editor.page.goal.GoalPageViewImpl;
import org.eclipse.che.ide.command.editor.page.name.NamePageView;
import org.eclipse.che.ide.command.editor.page.name.NamePageViewImpl;
import org.eclipse.che.ide.command.editor.page.project.ProjectsPageView;
import org.eclipse.che.ide.command.editor.page.project.ProjectsPageViewImpl;
import org.eclipse.che.ide.command.editor.page.text.PageWithTextEditorView;
import org.eclipse.che.ide.command.editor.page.text.PageWithTextEditorViewImpl;
import org.eclipse.che.ide.command.execute.CommandExecutorImpl;
import org.eclipse.che.ide.command.execute.ExecuteCommandActionFactory;
import org.eclipse.che.ide.command.execute.ExecuteCommandActionManager;
import org.eclipse.che.ide.command.execute.GoalPopUpGroupFactory;
import org.eclipse.che.ide.command.explorer.CommandsExplorerView;
import org.eclipse.che.ide.command.explorer.CommandsExplorerViewImpl;
import org.eclipse.che.ide.command.goal.BuildGoal;
import org.eclipse.che.ide.command.goal.CommandGoalRegistryImpl;
import org.eclipse.che.ide.command.goal.CommonGoal;
import org.eclipse.che.ide.command.goal.DebugGoal;
import org.eclipse.che.ide.command.goal.DeployGoal;
import org.eclipse.che.ide.command.goal.RunGoal;
import org.eclipse.che.ide.command.goal.TestGoal;
import org.eclipse.che.ide.command.manager.CommandManagerImpl;
import org.eclipse.che.ide.command.node.NodeFactory;
import org.eclipse.che.ide.command.palette.CommandsPaletteView;
import org.eclipse.che.ide.command.palette.CommandsPaletteViewImpl;
import org.eclipse.che.ide.command.producer.CommandProducerActionFactory;
import org.eclipse.che.ide.command.producer.CommandProducerActionManager;
import org.eclipse.che.ide.command.toolbar.CommandToolbarView;
import org.eclipse.che.ide.command.toolbar.CommandToolbarViewImpl;
import org.eclipse.che.ide.command.toolbar.ToolbarButtonsFactory;
import org.eclipse.che.ide.command.toolbar.commands.ExecuteCommandView;
import org.eclipse.che.ide.command.toolbar.commands.ExecuteCommandViewImpl;
import org.eclipse.che.ide.command.toolbar.commands.button.MenuItemsFactory;
import org.eclipse.che.ide.command.toolbar.controller.ToolbarControllerView;
import org.eclipse.che.ide.command.toolbar.controller.ToolbarControllerViewImpl;
import org.eclipse.che.ide.command.toolbar.previews.PreviewsView;
import org.eclipse.che.ide.command.toolbar.previews.PreviewsViewImpl;
import org.eclipse.che.ide.command.toolbar.processes.ProcessesListView;
import org.eclipse.che.ide.command.toolbar.processes.ProcessesListViewImpl;
import org.eclipse.che.ide.command.toolbar.selector.PanelSelectorView;
import org.eclipse.che.ide.command.toolbar.selector.PanelSelectorViewImpl;
import org.eclipse.che.ide.command.type.CommandTypeRegistryImpl;
import org.eclipse.che.ide.command.type.chooser.CommandTypeChooserView;
import org.eclipse.che.ide.command.type.chooser.CommandTypeChooserViewImpl;
import org.eclipse.che.ide.command.type.custom.CustomCommandType;

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
    GinMultibinder<CommandGoal> goalBinder =
        GinMultibinder.newSetBinder(binder(), CommandGoal.class);
    goalBinder.addBinding().to(BuildGoal.class);
    goalBinder.addBinding().to(TestGoal.class);
    goalBinder.addBinding().to(RunGoal.class);
    goalBinder.addBinding().to(DebugGoal.class);
    goalBinder.addBinding().to(DeployGoal.class);
    goalBinder.addBinding().to(CommonGoal.class);

    bind(CommandTypeRegistry.class).to(CommandTypeRegistryImpl.class).in(Singleton.class);
    bind(CommandGoalRegistry.class).to(CommandGoalRegistryImpl.class).in(Singleton.class);

    bind(CommandManager.class).to(CommandManagerImpl.class).in(Singleton.class);
    bind(CommandManager.class).asEagerSingleton();
    bind(ExecuteCommandActionManager.class).asEagerSingleton();

    GinMultibinder<CommandProducer> commandProducerBinder =
        GinMultibinder.newSetBinder(binder(), CommandProducer.class);
    bind(CommandProducerActionManager.class).asEagerSingleton();

    install(new GinFactoryModuleBuilder().build(ExecuteCommandActionFactory.class));
    install(new GinFactoryModuleBuilder().build(GoalPopUpGroupFactory.class));
    install(new GinFactoryModuleBuilder().build(NodeFactory.class));
    install(new GinFactoryModuleBuilder().build(CommandProducerActionFactory.class));

    bind(CommandsExplorerView.class).to(CommandsExplorerViewImpl.class).in(Singleton.class);
    bind(CommandTypeChooserView.class).to(CommandTypeChooserViewImpl.class);
    bind(CommandsPaletteView.class).to(CommandsPaletteViewImpl.class).in(Singleton.class);

    // command editor
    bind(CommandEditorView.class).to(CommandEditorViewImpl.class);
    bind(NamePageView.class).to(NamePageViewImpl.class);
    bind(GoalPageView.class).to(GoalPageViewImpl.class);
    bind(ProjectsPageView.class).to(ProjectsPageViewImpl.class);
    bind(PageWithTextEditorView.class).to(PageWithTextEditorViewImpl.class);

    // toolbar
    bind(CommandToolbarView.class).to(CommandToolbarViewImpl.class).in(Singleton.class);
    bind(ExecuteCommandView.class).to(ExecuteCommandViewImpl.class).in(Singleton.class);
    bind(ProcessesListView.class).to(ProcessesListViewImpl.class).in(Singleton.class);
    bind(PreviewsView.class).to(PreviewsViewImpl.class).in(Singleton.class);

    // Panel selector
    bind(PanelSelectorView.class).to(PanelSelectorViewImpl.class).in(Singleton.class);

    // Toolbar controller
    bind(ToolbarControllerView.class).to(ToolbarControllerViewImpl.class).in(Singleton.class);

    install(new GinFactoryModuleBuilder().build(ToolbarButtonsFactory.class));
    install(new GinFactoryModuleBuilder().build(MenuItemsFactory.class));

    bind(CommandExecutor.class).to(CommandExecutorImpl.class).in(Singleton.class);
    GinMultibinder.newSetBinder(binder(), CommandType.class)
        .addBinding()
        .to(CustomCommandType.class);
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
