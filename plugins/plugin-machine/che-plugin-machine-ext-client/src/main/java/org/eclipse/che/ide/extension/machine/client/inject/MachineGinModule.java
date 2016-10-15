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

import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.machine.MachineManager;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.extension.machine.client.RecipeScriptDownloadServiceClient;
import org.eclipse.che.ide.extension.machine.client.RecipeScriptDownloadServiceClientImpl;
import org.eclipse.che.ide.extension.machine.client.command.CommandManagerImpl;
import org.eclipse.che.ide.extension.machine.client.command.custom.CustomCommandType;
import org.eclipse.che.ide.extension.machine.client.command.edit.EditCommandsView;
import org.eclipse.che.ide.extension.machine.client.command.edit.EditCommandsViewImpl;
import org.eclipse.che.ide.extension.machine.client.command.macros.CurrentProjectPathMacro;
import org.eclipse.che.ide.extension.machine.client.command.macros.CurrentProjectRelativePathMacro;
import org.eclipse.che.ide.extension.machine.client.command.macros.DevMachineHostNameMacro;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.inject.factories.TerminalFactory;
import org.eclipse.che.ide.extension.machine.client.inject.factories.WidgetsFactory;
import org.eclipse.che.ide.extension.machine.client.machine.MachineEntityImpl;
import org.eclipse.che.ide.extension.machine.client.machine.MachineManagerImpl;
import org.eclipse.che.ide.extension.machine.client.machine.create.CreateMachineView;
import org.eclipse.che.ide.extension.machine.client.machine.create.CreateMachineViewImpl;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsole;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsolePresenter;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.DefaultOutputConsole;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.OutputConsoleView;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.OutputConsoleViewImpl;
import org.eclipse.che.ide.extension.machine.client.perspective.OperationsPerspective;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.button.EditorButtonWidget;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.button.EditorButtonWidgetImpl;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.Tab;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.TabImpl;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header.TabHeader;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header.TabHeaderImpl;
import org.eclipse.che.ide.extension.machine.client.processes.actions.ConsoleTreeContextMenuFactory;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelView;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelViewImpl;
import org.eclipse.che.ide.extension.machine.client.targets.BaseTarget;
import org.eclipse.che.ide.extension.machine.client.targets.CategoryPage;
import org.eclipse.che.ide.extension.machine.client.targets.Target;
import org.eclipse.che.ide.extension.machine.client.targets.TargetsView;
import org.eclipse.che.ide.extension.machine.client.targets.TargetsViewImpl;
import org.eclipse.che.ide.extension.machine.client.targets.categories.development.DevelopmentCategoryPresenter;
import org.eclipse.che.ide.extension.machine.client.targets.categories.development.DevelopmentView;
import org.eclipse.che.ide.extension.machine.client.targets.categories.development.DevelopmentViewImpl;
import org.eclipse.che.ide.extension.machine.client.targets.categories.docker.DockerCategoryPresenter;
import org.eclipse.che.ide.extension.machine.client.targets.categories.docker.DockerView;
import org.eclipse.che.ide.extension.machine.client.targets.categories.docker.DockerViewImpl;
import org.eclipse.che.ide.extension.machine.client.targets.categories.ssh.SshCategoryPresenter;
import org.eclipse.che.ide.extension.machine.client.targets.categories.ssh.SshView;
import org.eclipse.che.ide.extension.machine.client.targets.categories.ssh.SshViewImpl;

import static org.eclipse.che.ide.extension.machine.client.perspective.OperationsPerspective.OPERATIONS_PERSPECTIVE_ID;

/**
 * GIN module for Machine extension.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@ExtensionGinModule
public class MachineGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        GinMapBinder.newMapBinder(binder(), String.class, Perspective.class)
                    .addBinding(OPERATIONS_PERSPECTIVE_ID)
                    .to(OperationsPerspective.class);

        bind(CreateMachineView.class).to(CreateMachineViewImpl.class);
        bind(OutputConsoleView.class).to(OutputConsoleViewImpl.class);
        install(new GinFactoryModuleBuilder()
                        .implement(CommandOutputConsole.class, Names.named("command"), CommandOutputConsolePresenter.class)
                        .implement(OutputConsole.class, Names.named("default"), DefaultOutputConsole.class)
                        .build(CommandConsoleFactory.class));

        bind(ProcessesPanelView.class).to(ProcessesPanelViewImpl.class).in(Singleton.class);

        bind(CommandManager.class).to(CommandManagerImpl.class).in(Singleton.class);
        bind(EditCommandsView.class).to(EditCommandsViewImpl.class).in(Singleton.class);

        bind(TargetsView.class).to(TargetsViewImpl.class).in(Singleton.class);

        GinMultibinder.newSetBinder(binder(), CommandType.class).addBinding().to(CustomCommandType.class);

        final GinMultibinder<Macro> macrosBinder = GinMultibinder.newSetBinder(binder(), Macro.class);
        macrosBinder.addBinding().to(DevMachineHostNameMacro.class);
        macrosBinder.addBinding().to(CurrentProjectPathMacro.class);
        macrosBinder.addBinding().to(CurrentProjectRelativePathMacro.class);

        install(new GinFactoryModuleBuilder().implement(TabHeader.class, TabHeaderImpl.class)
                                             .implement(EditorButtonWidget.class, EditorButtonWidgetImpl.class)
                                             .build(WidgetsFactory.class));
        install(new GinFactoryModuleBuilder().implement(Tab.class, TabImpl.class)
                                             .implement(MachineEntity.class, MachineEntityImpl.class)
                                             .build(EntityFactory.class));
        install(new GinFactoryModuleBuilder().build(TerminalFactory.class));

        bind(MachineManager.class).to(MachineManagerImpl.class).in(Singleton.class);

        bindConstant().annotatedWith(Names.named("machine.extension.api_port")).to(Constants.WS_AGENT_PORT);

        bind(SshView.class).to(SshViewImpl.class);
        bind(DockerView.class).to(DockerViewImpl.class);
        bind(DevelopmentView.class).to(DevelopmentViewImpl.class);

        bind(Target.class).to(BaseTarget.class);

        bind(RecipeScriptDownloadServiceClient.class).to(RecipeScriptDownloadServiceClientImpl.class).in(Singleton.class);

        final GinMultibinder<CategoryPage> categoryPageBinder = GinMultibinder.newSetBinder(binder(), CategoryPage.class);
        categoryPageBinder.addBinding().to(SshCategoryPresenter.class);
        categoryPageBinder.addBinding().to(DockerCategoryPresenter.class);
        categoryPageBinder.addBinding().to(DevelopmentCategoryPresenter.class);

        install(new GinFactoryModuleBuilder().build(ConsoleTreeContextMenuFactory.class));
    }
}
