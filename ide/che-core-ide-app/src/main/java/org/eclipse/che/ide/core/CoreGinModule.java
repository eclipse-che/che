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
package org.eclipse.che.ide.core;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import elemental.json.Json;
import elemental.json.JsonFactory;
import org.eclipse.che.ide.QueryParameters;
import org.eclipse.che.ide.actions.ActionApiModule;
import org.eclipse.che.ide.api.ProductInfoDataProvider;
import org.eclipse.che.ide.api.ProductInfoDataProviderImpl;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.exec.ConnectedEventHandler;
import org.eclipse.che.ide.api.command.exec.ExecAgentCommandManager;
import org.eclipse.che.ide.api.command.exec.ExecAgentEventManager;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.extension.ExtensionRegistry;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.reference.FqnProvider;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.api.ssh.SshServiceClient;
import org.eclipse.che.ide.api.vcs.VcsChangeMarkerRenderFactory;
import org.eclipse.che.ide.clipboard.ClipboardModule;
import org.eclipse.che.ide.command.CommandApiModule;
import org.eclipse.che.ide.command.execute.JsonRpcExecAgentCommandManager;
import org.eclipse.che.ide.command.execute.JsonRpcExecAgentEventManager;
import org.eclipse.che.ide.console.ConsoleGinModule;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.debug.DebugApiModule;
import org.eclipse.che.ide.dto.DtoModule;
import org.eclipse.che.ide.editor.EditorApiModule;
import org.eclipse.che.ide.editor.preferences.EditorPreferencesModule;
import org.eclipse.che.ide.factory.FactoryGinModule;
import org.eclipse.che.ide.filetypes.FileTypeApiModule;
import org.eclipse.che.ide.jsonrpc.JsonRpcModule;
import org.eclipse.che.ide.keybinding.KeyBindingManager;
import org.eclipse.che.ide.machine.MachineApiModule;
import org.eclipse.che.ide.macro.MacroApiModule;
import org.eclipse.che.ide.notification.NotificationApiModule;
import org.eclipse.che.ide.oauth.OAuthApiModule;
import org.eclipse.che.ide.part.PartApiModule;
import org.eclipse.che.ide.preferences.PreferencesApiModule;
import org.eclipse.che.ide.processes.ProcessesGinModule;
import org.eclipse.che.ide.processes.runtime.RuntimeInfoGinModule;
import org.eclipse.che.ide.project.ProjectApiModule;
import org.eclipse.che.ide.resources.ResourceApiModule;
import org.eclipse.che.ide.search.factory.FindResultNodeFactory;
import org.eclipse.che.ide.selection.SelectionAgentImpl;
import org.eclipse.che.ide.ssh.SshServiceClientImpl;
import org.eclipse.che.ide.statepersistance.PersistenceApiModule;
import org.eclipse.che.ide.terminal.TerminalFactory;
import org.eclipse.che.ide.terminal.TerminalInitializer;
import org.eclipse.che.ide.theme.ThemeApiModule;
import org.eclipse.che.ide.ui.dialogs.askcredentials.AskCredentialsDialog;
import org.eclipse.che.ide.ui.loaders.PopupLoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.user.AskCredentialsDialogImpl;
import org.eclipse.che.ide.workspace.WorkspaceApiModule;
import org.eclipse.che.ide.workspace.WorkspacePresenter;
import org.eclipse.che.ide.workspace.events.WorkspaceEventsModule;
import org.eclipse.che.providers.DynaProvider;
import org.eclipse.che.providers.DynaProviderImpl;

/**
 * @author Nikolay Zamosenchuk
 * @author Dmitry Shnurenko
 */
@ExtensionGinModule
public class CoreGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    install(new JsonRpcModule());
    install(new WebSocketModule());
    install(new ClientServerEventModule());

    install(new DtoModule());

    install(new ThemeApiModule());
    install(new UiModule());
    install(new ClipboardModule());

    bind(AppContextImpl.class).asEagerSingleton();
    bind(QueryParameters.class).asEagerSingleton();

    install(new EditorApiModule());
    install(new EditorPreferencesModule());
    install(new NotificationApiModule());
    install(new FileTypeApiModule());
    install(new ResourceApiModule());
    install(new ActionApiModule());
    install(new PartApiModule());
    install(new DebugApiModule());
    install(new PreferencesApiModule());
    install(new PersistenceApiModule());
    install(new MacroApiModule());
    install(new MachineApiModule());
    install(new CommandApiModule());
    install(new ConsoleGinModule());
    install(new ProcessesGinModule());
    install(new ProjectApiModule());
    install(new OAuthApiModule());
    install(new WorkspaceEventsModule());
    install(new WorkspaceApiModule());
    install(new FactoryGinModule());
    install(new RuntimeInfoGinModule());

    // configure miscellaneous core components
    bind(StandardComponentInitializer.class).in(Singleton.class);

    bind(TerminalInitializer.class).in(Singleton.class);

    bind(DynaProvider.class).to(DynaProviderImpl.class);

    GinMapBinder.newMapBinder(binder(), String.class, FqnProvider.class);

    GinMapBinder.newMapBinder(binder(), String.class, VcsChangeMarkerRenderFactory.class);

    bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);

    install(new GinFactoryModuleBuilder().build(LoaderFactory.class));
    install(new GinFactoryModuleBuilder().build(PopupLoaderFactory.class));

    bind(ExtensionRegistry.class).in(Singleton.class);

    bind(AppContext.class).to(AppContextImpl.class);

    install(new GinFactoryModuleBuilder().build(FindResultNodeFactory.class));
    install(new GinFactoryModuleBuilder().build(TerminalFactory.class));

    // clients for the REST services
    bind(SshServiceClient.class).to(SshServiceClientImpl.class).in(Singleton.class);

    // IDE agents
    bind(SelectionAgent.class).to(SelectionAgentImpl.class).asEagerSingleton();
    bind(KeyBindingAgent.class).to(KeyBindingManager.class).in(Singleton.class);
    bind(WorkspaceAgent.class).to(WorkspacePresenter.class).in(Singleton.class);

    // Exec agent
    bind(ExecAgentCommandManager.class).to(JsonRpcExecAgentCommandManager.class);
    bind(ExecAgentEventManager.class).to(JsonRpcExecAgentEventManager.class);
    bind(ConnectedEventHandler.class).asEagerSingleton();

    bind(AskCredentialsDialog.class).to(AskCredentialsDialogImpl.class);
    bind(ProductInfoDataProvider.class).to(ProductInfoDataProviderImpl.class);
  }

  @Provides
  @Singleton
  protected JsonFactory provideJsonFactory() {
    return Json.instance();
  }
}
