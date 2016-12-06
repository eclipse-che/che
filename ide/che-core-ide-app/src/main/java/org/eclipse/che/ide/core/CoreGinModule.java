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
package org.eclipse.che.ide.core;

import elemental.json.Json;
import elemental.json.JsonFactory;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

import org.eclipse.che.ide.actions.ActionApiModule;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.extension.ExtensionRegistry;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.git.GitServiceClientImpl;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.machine.MachineServiceClientImpl;
import org.eclipse.che.ide.api.machine.RecipeServiceClient;
import org.eclipse.che.ide.api.machine.RecipeServiceClientImpl;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.reference.FqnProvider;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.api.ssh.SshServiceClient;
import org.eclipse.che.ide.api.ssh.SshServiceClientImpl;
import org.eclipse.che.ide.clipboard.ClipboardModule;
import org.eclipse.che.ide.command.CommandApiModule;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.debug.DebugApiModule;
import org.eclipse.che.ide.editor.EditorApiModule;
import org.eclipse.che.ide.editor.preferences.EditorPreferencesModule;
import org.eclipse.che.ide.factory.FactoryApiModule;
import org.eclipse.che.ide.filetypes.FileTypeApiModule;
import org.eclipse.che.ide.keybinding.KeyBindingManager;
import org.eclipse.che.ide.macro.MacroApiModule;
import org.eclipse.che.ide.notification.NotificationApiModule;
import org.eclipse.che.ide.oauth.OAuthApiModule;
import org.eclipse.che.ide.part.PartApiModule;
import org.eclipse.che.ide.preferences.PreferencesApiModule;
import org.eclipse.che.ide.project.ProjectApiModule;
import org.eclipse.che.ide.projectimport.ProjectImportModule;
import org.eclipse.che.ide.resources.ResourceApiModule;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.ide.rest.RestContextProvider;
import org.eclipse.che.ide.search.factory.FindResultNodeFactory;
import org.eclipse.che.ide.selection.SelectionAgentImpl;
import org.eclipse.che.ide.statepersistance.PersistenceApiModule;
import org.eclipse.che.ide.theme.ThemeApiModule;
import org.eclipse.che.ide.ui.loaders.PopupLoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.user.UserApiModule;
import org.eclipse.che.ide.workspace.WorkspaceApiModule;
import org.eclipse.che.ide.workspace.WorkspacePresenter;
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

        install(new UiModule());
        install(new ClipboardModule());

        install(new EditorApiModule());
        install(new EditorPreferencesModule());
        install(new NotificationApiModule());
        install(new FileTypeApiModule());
        install(new ResourceApiModule());
        install(new ActionApiModule());
        install(new PartApiModule());
        install(new DebugApiModule());
        install(new ThemeApiModule());
        install(new PreferencesApiModule());
        install(new PersistenceApiModule());
        install(new MacroApiModule());
        install(new UserApiModule());
        install(new WorkspaceApiModule());
        install(new CommandApiModule());
        install(new ProjectApiModule());
        install(new ProjectImportModule());
        install(new OAuthApiModule());
        install(new FactoryApiModule());

        // configure miscellaneous core components
        bind(StandardComponentInitializer.class).in(Singleton.class);

        GinMapBinder<String, Component> componentsBinder = GinMapBinder.newMapBinder(binder(), String.class, Component.class);
        componentsBinder.addBinding("Standard components").to(StandardComponent.class);

        bind(DynaProvider.class).to(DynaProviderImpl.class);

        GinMapBinder.newMapBinder(binder(), String.class, FqnProvider.class);

        bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);

        bind(String.class).annotatedWith(RestContext.class).toProvider(RestContextProvider.class).in(Singleton.class);

        install(new GinFactoryModuleBuilder().build(LoaderFactory.class));
        install(new GinFactoryModuleBuilder().build(PopupLoaderFactory.class));

        bind(ExtensionRegistry.class).in(Singleton.class);

        bind(AppContext.class).to(AppContextImpl.class);

        install(new GinFactoryModuleBuilder().build(FindResultNodeFactory.class));

        // clients for the REST services
        bind(GitServiceClient.class).to(GitServiceClientImpl.class).in(Singleton.class);
        bind(SshServiceClient.class).to(SshServiceClientImpl.class).in(Singleton.class);
        bind(RecipeServiceClient.class).to(RecipeServiceClientImpl.class).in(Singleton.class);
        bind(MachineServiceClient.class).to(MachineServiceClientImpl.class).in(Singleton.class);

        // IDE agents
        bind(SelectionAgent.class).to(SelectionAgentImpl.class).asEagerSingleton();
        bind(KeyBindingAgent.class).to(KeyBindingManager.class).in(Singleton.class);
        bind(WorkspaceAgent.class).to(WorkspacePresenter.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    protected JsonFactory provideJsonFactory() {
        return Json.instance();
    }
}
