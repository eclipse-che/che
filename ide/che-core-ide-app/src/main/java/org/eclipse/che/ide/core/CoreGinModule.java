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

import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.actions.ActionManagerImpl;
import org.eclipse.che.ide.actions.find.FindActionView;
import org.eclipse.che.ide.actions.find.FindActionViewImpl;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.auth.OAuthServiceClient;
import org.eclipse.che.ide.api.auth.OAuthServiceClientImpl;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.component.StateComponent;
import org.eclipse.che.ide.api.component.WsAgentComponent;
import org.eclipse.che.ide.api.data.tree.NodeInterceptor;
import org.eclipse.che.ide.api.data.tree.settings.SettingsProvider;
import org.eclipse.che.ide.api.data.tree.settings.impl.DummySettingsProvider;
import org.eclipse.che.ide.api.debug.DebuggerServiceClient;
import org.eclipse.che.ide.api.debug.DebuggerServiceClientImpl;
import org.eclipse.che.ide.api.dialogs.ChoiceDialog;
import org.eclipse.che.ide.api.dialogs.ConfirmDialog;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.InputDialog;
import org.eclipse.che.ide.api.dialogs.MessageDialog;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.event.ng.ClientServerEventService;
import org.eclipse.che.ide.api.event.ng.EditorFileStatusNotificationReceiver;
import org.eclipse.che.ide.api.event.ng.FileOpenCloseEventListener;
import org.eclipse.che.ide.api.event.ng.JsonRpcWebSocketAgentEventListener;
import org.eclipse.che.ide.api.event.ng.ProjectTreeStatusNotificationReceiver;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.extension.ExtensionRegistry;
import org.eclipse.che.ide.api.factory.FactoryServiceClient;
import org.eclipse.che.ide.api.factory.FactoryServiceClientImpl;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.git.GitServiceClientImpl;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.machine.MachineServiceClientImpl;
import org.eclipse.che.ide.api.machine.RecipeServiceClient;
import org.eclipse.che.ide.api.machine.RecipeServiceClientImpl;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.macro.MacroRegistry;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.oauth.OAuth2Authenticator;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorRegistry;
import org.eclipse.che.ide.api.parts.EditorMultiPartStack;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.api.parts.PerspectiveView;
import org.eclipse.che.ide.api.parts.ProjectExplorerPart;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.project.ProjectImportersServiceClient;
import org.eclipse.che.ide.api.project.ProjectImportersServiceClientImpl;
import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.ide.api.project.ProjectServiceClientImpl;
import org.eclipse.che.ide.api.project.ProjectTemplateServiceClient;
import org.eclipse.che.ide.api.project.ProjectTemplateServiceClientImpl;
import org.eclipse.che.ide.api.project.ProjectTypeServiceClient;
import org.eclipse.che.ide.api.project.ProjectTypeServiceClientImpl;
import org.eclipse.che.ide.api.project.type.ProjectTemplateRegistry;
import org.eclipse.che.ide.api.project.type.ProjectTypeRegistry;
import org.eclipse.che.ide.api.project.type.wizard.PreSelectedProjectTypeManager;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistry;
import org.eclipse.che.ide.api.project.wizard.ImportProjectNotificationSubscriberFactory;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistrar;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistry;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.api.reference.FqnProvider;
import org.eclipse.che.ide.api.resources.ResourceInterceptor;
import org.eclipse.che.ide.api.resources.modification.ClipboardManager;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.api.ssh.SshServiceClient;
import org.eclipse.che.ide.api.ssh.SshServiceClientImpl;
import org.eclipse.che.ide.api.theme.Theme;
import org.eclipse.che.ide.api.theme.ThemeAgent;
import org.eclipse.che.ide.api.user.PreferencesServiceClient;
import org.eclipse.che.ide.api.user.PreferencesServiceClientImpl;
import org.eclipse.che.ide.api.user.UserProfileServiceClient;
import org.eclipse.che.ide.api.user.UserProfileServiceClientImpl;
import org.eclipse.che.ide.api.user.UserServiceClient;
import org.eclipse.che.ide.api.user.UserServiceClientImpl;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClientImpl;
import org.eclipse.che.ide.client.StartUpActionsProcessor;
import org.eclipse.che.ide.client.WorkspaceStateRestorer;
import org.eclipse.che.ide.command.CommandProducerActionFactory;
import org.eclipse.che.ide.command.CommandProducerActionManager;
import org.eclipse.che.ide.command.CommandTypeRegistryImpl;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.editor.EditorAgentImpl;
import org.eclipse.che.ide.editor.EditorRegistryImpl;
import org.eclipse.che.ide.editor.macro.EditorCurrentFileNameMacro;
import org.eclipse.che.ide.editor.macro.EditorCurrentFilePathMacro;
import org.eclipse.che.ide.editor.macro.EditorCurrentFileRelativePathMacro;
import org.eclipse.che.ide.editor.macro.EditorCurrentProjectNameMacro;
import org.eclipse.che.ide.editor.macro.EditorCurrentProjectTypeMacro;
import org.eclipse.che.ide.editor.synchronization.EditorContentSynchronizer;
import org.eclipse.che.ide.editor.synchronization.EditorContentSynchronizerImpl;
import org.eclipse.che.ide.editor.synchronization.EditorGroupSychronizationFactory;
import org.eclipse.che.ide.editor.synchronization.EditorGroupSynchronization;
import org.eclipse.che.ide.editor.synchronization.EditorGroupSynchronizationImpl;
import org.eclipse.che.ide.filetypes.FileTypeRegistryImpl;
import org.eclipse.che.ide.hotkeys.dialog.HotKeysDialogView;
import org.eclipse.che.ide.hotkeys.dialog.HotKeysDialogViewImpl;
import org.eclipse.che.ide.icon.DefaultIconsComponent;
import org.eclipse.che.ide.icon.IconRegistryImpl;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestReceiver;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestTransmitter;
import org.eclipse.che.ide.jsonrpc.JsonRpcResponseReceiver;
import org.eclipse.che.ide.jsonrpc.JsonRpcResponseTransmitter;
import org.eclipse.che.ide.jsonrpc.impl.BasicJsonRpcObjectValidator;
import org.eclipse.che.ide.jsonrpc.impl.JsonRpcDispatcher;
import org.eclipse.che.ide.jsonrpc.impl.JsonRpcInitializer;
import org.eclipse.che.ide.jsonrpc.impl.JsonRpcObjectValidator;
import org.eclipse.che.ide.jsonrpc.impl.WebSocketJsonRpcDispatcher;
import org.eclipse.che.ide.jsonrpc.impl.WebSocketJsonRpcInitializer;
import org.eclipse.che.ide.jsonrpc.impl.WebSocketJsonRpcRequestDispatcher;
import org.eclipse.che.ide.jsonrpc.impl.WebSocketJsonRpcRequestTransmitter;
import org.eclipse.che.ide.jsonrpc.impl.WebSocketJsonRpcResponseDispatcher;
import org.eclipse.che.ide.jsonrpc.impl.WebSocketJsonRpcResponseTransmitter;
import org.eclipse.che.ide.keybinding.KeyBindingManager;
import org.eclipse.che.ide.macro.MacroProcessorImpl;
import org.eclipse.che.ide.macro.MacroRegistryImpl;
import org.eclipse.che.ide.menu.MainMenuView;
import org.eclipse.che.ide.menu.MainMenuViewImpl;
import org.eclipse.che.ide.menu.StatusPanelGroupView;
import org.eclipse.che.ide.menu.StatusPanelGroupViewImpl;
import org.eclipse.che.ide.navigation.NavigateToFileView;
import org.eclipse.che.ide.navigation.NavigateToFileViewImpl;
import org.eclipse.che.ide.notification.NotificationManagerImpl;
import org.eclipse.che.ide.notification.NotificationManagerView;
import org.eclipse.che.ide.notification.NotificationManagerViewImpl;
import org.eclipse.che.ide.oauth.DefaultOAuthAuthenticatorImpl;
import org.eclipse.che.ide.oauth.OAuth2AuthenticatorRegistryImpl;
import org.eclipse.che.ide.part.FocusManager;
import org.eclipse.che.ide.part.PartStackPresenter;
import org.eclipse.che.ide.part.PartStackPresenter.PartStackEventHandler;
import org.eclipse.che.ide.part.PartStackViewImpl;
import org.eclipse.che.ide.part.editor.EditorPartStackView;
import org.eclipse.che.ide.part.editor.EditorTabContextMenuFactory;
import org.eclipse.che.ide.part.editor.multipart.EditorMultiPartStackPresenter;
import org.eclipse.che.ide.part.editor.recent.RecentFileActionFactory;
import org.eclipse.che.ide.part.editor.recent.RecentFileList;
import org.eclipse.che.ide.part.editor.recent.RecentFileStore;
import org.eclipse.che.ide.part.explorer.project.DefaultNodeInterceptor;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerView;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerViewImpl;
import org.eclipse.che.ide.part.explorer.project.RevealNodesPersistenceComponent;
import org.eclipse.che.ide.part.explorer.project.TreeResourceRevealer;
import org.eclipse.che.ide.part.explorer.project.macro.ExplorerCurrentFileNameMacro;
import org.eclipse.che.ide.part.explorer.project.macro.ExplorerCurrentFileParentPathMacro;
import org.eclipse.che.ide.part.explorer.project.macro.ExplorerCurrentFilePathMacro;
import org.eclipse.che.ide.part.explorer.project.macro.ExplorerCurrentFileRelativePathMacro;
import org.eclipse.che.ide.part.explorer.project.macro.ExplorerCurrentProjectNameMacro;
import org.eclipse.che.ide.part.explorer.project.macro.ExplorerCurrentProjectTypeMacro;
import org.eclipse.che.ide.preferences.PreferencesComponent;
import org.eclipse.che.ide.preferences.PreferencesManagerImpl;
import org.eclipse.che.ide.preferences.PreferencesView;
import org.eclipse.che.ide.preferences.PreferencesViewImpl;
import org.eclipse.che.ide.preferences.pages.appearance.AppearancePresenter;
import org.eclipse.che.ide.preferences.pages.appearance.AppearanceView;
import org.eclipse.che.ide.preferences.pages.appearance.AppearanceViewImpl;
import org.eclipse.che.ide.preferences.pages.extensions.ExtensionManagerPresenter;
import org.eclipse.che.ide.preferences.pages.extensions.ExtensionManagerView;
import org.eclipse.che.ide.preferences.pages.extensions.ExtensionManagerViewImpl;
import org.eclipse.che.ide.project.node.icon.DockerfileIconProvider;
import org.eclipse.che.ide.project.node.icon.FileIconProvider;
import org.eclipse.che.ide.project.node.icon.NodeIconProvider;
import org.eclipse.che.ide.projectimport.wizard.ImportWizardFactory;
import org.eclipse.che.ide.projectimport.wizard.ImportWizardRegistryImpl;
import org.eclipse.che.ide.projectimport.wizard.ProjectNotificationSubscriberImpl;
import org.eclipse.che.ide.projectimport.zip.ZipImportWizardRegistrar;
import org.eclipse.che.ide.projecttype.BlankProjectWizardRegistrar;
import org.eclipse.che.ide.projecttype.ProjectTemplateRegistryImpl;
import org.eclipse.che.ide.projecttype.ProjectTemplatesComponent;
import org.eclipse.che.ide.projecttype.ProjectTypeComponent;
import org.eclipse.che.ide.projecttype.ProjectTypeRegistryImpl;
import org.eclipse.che.ide.projecttype.wizard.PreSelectedProjectTypeManagerImpl;
import org.eclipse.che.ide.projecttype.wizard.ProjectWizardFactory;
import org.eclipse.che.ide.projecttype.wizard.ProjectWizardRegistryImpl;
import org.eclipse.che.ide.resources.impl.ClipboardManagerImpl;
import org.eclipse.che.ide.resources.impl.ResourceManager;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.ide.rest.RestContextProvider;
import org.eclipse.che.ide.search.factory.FindResultNodeFactory;
import org.eclipse.che.ide.selection.SelectionAgentImpl;
import org.eclipse.che.ide.statepersistance.OpenedFilesPersistenceComponent;
import org.eclipse.che.ide.statepersistance.PersistenceComponent;
import org.eclipse.che.ide.statepersistance.ShowHiddenFilesPersistenceComponent;
import org.eclipse.che.ide.theme.DarkTheme;
import org.eclipse.che.ide.theme.LightTheme;
import org.eclipse.che.ide.theme.ThemeAgentImpl;
import org.eclipse.che.ide.ui.button.ConsoleButton;
import org.eclipse.che.ide.ui.button.ConsoleButtonFactory;
import org.eclipse.che.ide.ui.button.ConsoleButtonImpl;
import org.eclipse.che.ide.ui.dialogs.choice.ChoiceDialogFooter;
import org.eclipse.che.ide.ui.dialogs.choice.ChoiceDialogPresenter;
import org.eclipse.che.ide.ui.dialogs.choice.ChoiceDialogView;
import org.eclipse.che.ide.ui.dialogs.choice.ChoiceDialogViewImpl;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialogFooter;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialogPresenter;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialogView;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialogViewImpl;
import org.eclipse.che.ide.ui.dialogs.input.InputDialogFooter;
import org.eclipse.che.ide.ui.dialogs.input.InputDialogPresenter;
import org.eclipse.che.ide.ui.dialogs.input.InputDialogView;
import org.eclipse.che.ide.ui.dialogs.input.InputDialogViewImpl;
import org.eclipse.che.ide.ui.dialogs.message.MessageDialogFooter;
import org.eclipse.che.ide.ui.dialogs.message.MessageDialogPresenter;
import org.eclipse.che.ide.ui.dialogs.message.MessageDialogView;
import org.eclipse.che.ide.ui.dialogs.message.MessageDialogViewImpl;
import org.eclipse.che.ide.ui.dropdown.DropDownListFactory;
import org.eclipse.che.ide.ui.dropdown.DropDownWidget;
import org.eclipse.che.ide.ui.dropdown.DropDownWidgetImpl;
import org.eclipse.che.ide.ui.loaders.PopupLoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanel;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanelFactory;
import org.eclipse.che.ide.ui.multisplitpanel.panel.SubPanelPresenter;
import org.eclipse.che.ide.ui.multisplitpanel.panel.SubPanelView;
import org.eclipse.che.ide.ui.multisplitpanel.panel.SubPanelViewFactory;
import org.eclipse.che.ide.ui.multisplitpanel.panel.SubPanelViewImpl;
import org.eclipse.che.ide.ui.multisplitpanel.tab.Tab;
import org.eclipse.che.ide.ui.multisplitpanel.tab.TabItemFactory;
import org.eclipse.che.ide.ui.multisplitpanel.tab.TabWidget;
import org.eclipse.che.ide.ui.toolbar.MainToolbar;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.eclipse.che.ide.ui.toolbar.ToolbarView;
import org.eclipse.che.ide.ui.toolbar.ToolbarViewImpl;
import org.eclipse.che.ide.ui.zeroclipboard.ClipboardButtonBuilder;
import org.eclipse.che.ide.ui.zeroclipboard.ClipboardButtonBuilderImpl;
import org.eclipse.che.ide.upload.file.UploadFileView;
import org.eclipse.che.ide.upload.file.UploadFileViewImpl;
import org.eclipse.che.ide.upload.folder.UploadFolderFromZipView;
import org.eclipse.che.ide.upload.folder.UploadFolderFromZipViewImpl;
import org.eclipse.che.ide.util.executor.UserActivityManager;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageReceiver;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;
import org.eclipse.che.ide.websocket.ng.impl.BasicWebSocketEndpoint;
import org.eclipse.che.ide.websocket.ng.impl.BasicWebSocketMessageTransmitter;
import org.eclipse.che.ide.websocket.ng.impl.BasicWebSocketTransmissionValidator;
import org.eclipse.che.ide.websocket.ng.impl.DelayableWebSocket;
import org.eclipse.che.ide.websocket.ng.impl.SessionWebSocketInitializer;
import org.eclipse.che.ide.websocket.ng.impl.WebSocket;
import org.eclipse.che.ide.websocket.ng.impl.WebSocketCreator;
import org.eclipse.che.ide.websocket.ng.impl.WebSocketEndpoint;
import org.eclipse.che.ide.websocket.ng.impl.WebSocketInitializer;
import org.eclipse.che.ide.websocket.ng.impl.WebSocketTransmissionValidator;
import org.eclipse.che.ide.workspace.PartStackPresenterFactory;
import org.eclipse.che.ide.workspace.PartStackViewFactory;
import org.eclipse.che.ide.workspace.WorkBenchControllerFactory;
import org.eclipse.che.ide.workspace.WorkBenchPartController;
import org.eclipse.che.ide.workspace.WorkBenchPartControllerImpl;
import org.eclipse.che.ide.workspace.WorkspaceComponentProvider;
import org.eclipse.che.ide.workspace.WorkspacePresenter;
import org.eclipse.che.ide.workspace.WorkspaceView;
import org.eclipse.che.ide.workspace.WorkspaceViewImpl;
import org.eclipse.che.ide.workspace.WorkspaceWidgetFactory;
import org.eclipse.che.ide.workspace.create.recipewidget.RecipeWidget;
import org.eclipse.che.ide.workspace.create.recipewidget.RecipeWidgetImpl;
import org.eclipse.che.ide.workspace.macro.WorkspaceNameMacro;
import org.eclipse.che.ide.workspace.perspectives.general.PerspectiveViewImpl;
import org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective;
import org.eclipse.che.ide.workspace.start.workspacewidget.WorkspaceWidget;
import org.eclipse.che.ide.workspace.start.workspacewidget.WorkspaceWidgetImpl;
import org.eclipse.che.providers.DynaProvider;
import org.eclipse.che.providers.DynaProviderImpl;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * @author Nikolay Zamosenchuk
 * @author Dmitry Shnurenko
 */
@ExtensionGinModule
public class CoreGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        GinMapBinder<String, Perspective> mapBinder = GinMapBinder.newMapBinder(binder(), String.class, Perspective.class);
        mapBinder.addBinding(PROJECT_PERSPECTIVE_ID).to(ProjectPerspective.class);

        GinMapBinder.newMapBinder(binder(), String.class, FqnProvider.class);

        install(new GinFactoryModuleBuilder().implement(WorkBenchPartController.class,
                                                        WorkBenchPartControllerImpl.class).build(WorkBenchControllerFactory.class));
        // generic bindings
        bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
        bind(Resources.class).in(Singleton.class);
        bind(String.class).annotatedWith(RestContext.class).toProvider(RestContextProvider.class).in(Singleton.class);
        bind(ExtensionRegistry.class).in(Singleton.class);
        bind(StandardComponentInitializer.class).in(Singleton.class);
        bind(ClipboardButtonBuilder.class).to(ClipboardButtonBuilderImpl.class);

        install(new GinFactoryModuleBuilder().build(ResourceManager.ResourceFactory.class));
        install(new GinFactoryModuleBuilder().build(ResourceManager.ResourceManagerFactory.class));
        install(new GinFactoryModuleBuilder().build(ResourceNode.NodeFactory.class));

        bind(AppContext.class).to(AppContextImpl.class);

        install(new GinFactoryModuleBuilder().build(LoaderFactory.class));
        install(new GinFactoryModuleBuilder().build(PopupLoaderFactory.class));

        install(new GinFactoryModuleBuilder().implement(PartStackView.class, PartStackViewImpl.class).build(PartStackViewFactory.class));
        install(new GinFactoryModuleBuilder().implement(PartStack.class, PartStackPresenter.class).build(PartStackPresenterFactory.class));

        bind(PreferencesManager.class).to(PreferencesManagerImpl.class);
        GinMultibinder.newSetBinder(binder(), PreferencesManager.class).addBinding().to(PreferencesManagerImpl.class);

        bind(NotificationManager.class).to(NotificationManagerImpl.class).in(Singleton.class);
        bind(ThemeAgent.class).to(ThemeAgentImpl.class).in(Singleton.class);
        bind(FileTypeRegistry.class).to(FileTypeRegistryImpl.class).in(Singleton.class);

        bind(DynaProvider.class).to(DynaProviderImpl.class);

        GinMultibinder.newSetBinder(binder(), OAuth2Authenticator.class).addBinding().to(DefaultOAuthAuthenticatorImpl.class);

        configureComponents();
        configureProjectWizard();
        configureImportWizard();
        configurePlatformApiGwtClients();
        configureApiBinding();
        configureCoreUI();
        configureEditorAPI();
        configureProjectTree();

        configureJsonRpc();
        configureWebSocket();
        configureClientServerEventService();

        GinMapBinder<String, StateComponent> stateComponents = GinMapBinder.newMapBinder(binder(), String.class, StateComponent.class);
        stateComponents.addBinding("workspace").to(WorkspacePresenter.class);
        stateComponents.addBinding("editor").to(EditorAgentImpl.class);

        GinMultibinder<PersistenceComponent> persistenceComponentsMultibinder =
                GinMultibinder.newSetBinder(binder(), PersistenceComponent.class);
        persistenceComponentsMultibinder.addBinding().to(ShowHiddenFilesPersistenceComponent.class);
        persistenceComponentsMultibinder.addBinding().to(OpenedFilesPersistenceComponent.class);
        persistenceComponentsMultibinder.addBinding().to(RevealNodesPersistenceComponent.class);

        install(new GinFactoryModuleBuilder().implement(RecipeWidget.class, RecipeWidgetImpl.class)
                                             .implement(WorkspaceWidget.class, WorkspaceWidgetImpl.class)
                                             .build(WorkspaceWidgetFactory.class));
        bind(StartUpActionsProcessor.class).in(Singleton.class);

        bind(ClipboardManager.class).to(ClipboardManagerImpl.class);

        GinMultibinder.newSetBinder(binder(), ResourceInterceptor.class).addBinding().to(ResourceInterceptor.NoOpInterceptor.class);
    }

    private void configureClientServerEventService() {
        bind(FileOpenCloseEventListener.class).asEagerSingleton();
        bind(ClientServerEventService.class).asEagerSingleton();

        GinMapBinder<String, JsonRpcRequestReceiver> requestReceivers =
                GinMapBinder.newMapBinder(binder(), String.class, JsonRpcRequestReceiver.class);

        requestReceivers.addBinding("event:file-in-vfs-status-changed").to(EditorFileStatusNotificationReceiver.class);
        requestReceivers.addBinding("event:project-tree-status-changed").to(ProjectTreeStatusNotificationReceiver.class);
    }

    private void configureJsonRpc() {
        bind(JsonRpcWebSocketAgentEventListener.class).asEagerSingleton();

        bind(JsonRpcInitializer.class).to(WebSocketJsonRpcInitializer.class);

        bind(JsonRpcRequestTransmitter.class).to(WebSocketJsonRpcRequestTransmitter.class);
        bind(JsonRpcResponseTransmitter.class).to(WebSocketJsonRpcResponseTransmitter.class);

        bind(JsonRpcObjectValidator.class).to(BasicJsonRpcObjectValidator.class);

        GinMapBinder<String, JsonRpcDispatcher> dispatchers = GinMapBinder.newMapBinder(binder(), String.class, JsonRpcDispatcher.class);
        dispatchers.addBinding("request").to(WebSocketJsonRpcRequestDispatcher.class);
        dispatchers.addBinding("response").to(WebSocketJsonRpcResponseDispatcher.class);

        GinMapBinder<String, JsonRpcRequestReceiver> requestReceivers =
                GinMapBinder.newMapBinder(binder(), String.class, JsonRpcRequestReceiver.class);

        GinMapBinder<String, JsonRpcResponseReceiver> responseReceivers =
                GinMapBinder.newMapBinder(binder(), String.class, JsonRpcResponseReceiver.class);
    }

    private void configureWebSocket() {
        bind(WebSocketInitializer.class).to(SessionWebSocketInitializer.class);

        bind(WebSocketEndpoint.class).to(BasicWebSocketEndpoint.class);

        bind(WebSocketTransmissionValidator.class).to(BasicWebSocketTransmissionValidator.class);

        bind(WebSocketMessageTransmitter.class).to(BasicWebSocketMessageTransmitter.class);

        install(new GinFactoryModuleBuilder()
                        .implement(WebSocket.class, DelayableWebSocket.class)
                        .build(WebSocketCreator.class));

        GinMapBinder<String, WebSocketMessageReceiver> receivers =
                GinMapBinder.newMapBinder(binder(), String.class, WebSocketMessageReceiver.class);

        receivers.addBinding("jsonrpc-2.0").to(WebSocketJsonRpcDispatcher.class);
    }

    private void configureComponents() {
        GinMapBinder<String, Component> componentsBinder = GinMapBinder.newMapBinder(binder(), String.class, Component.class);
        componentsBinder.addBinding("Default Icons").to(DefaultIconsComponent.class);
        componentsBinder.addBinding("Font Awesome Icons").to(FontAwesomeInjector.class);
        componentsBinder.addBinding("ZeroClipboard").to(ZeroClipboardInjector.class);
        componentsBinder.addBinding("Preferences").to(PreferencesComponent.class);
        componentsBinder.addBinding("Profile").to(ProfileComponent.class);
        componentsBinder.addBinding("Project templates").to(ProjectTemplatesComponent.class);
        componentsBinder.addBinding("Workspace").toProvider(WorkspaceComponentProvider.class);
        componentsBinder.addBinding("Standard components").to(StandardComponent.class);
        componentsBinder.addBinding("Contextual Commands").to(CommandProducerActionManager.class);

        GinMapBinder<String, WsAgentComponent> wsAgentComponentsBinder =
                GinMapBinder.newMapBinder(binder(), String.class, WsAgentComponent.class);
        wsAgentComponentsBinder.addBinding("Project types").to(ProjectTypeComponent.class);
        wsAgentComponentsBinder.addBinding("Start-up actions processor").to(StartUpActionsProcessor.class);
        wsAgentComponentsBinder.addBinding("ZZ Restore Workspace State").to(WorkspaceStateRestorer.class);
    }

    private void configureProjectWizard() {
        GinMultibinder.newSetBinder(binder(), ProjectWizardRegistrar.class).addBinding().to(BlankProjectWizardRegistrar.class);
        bind(ProjectWizardRegistry.class).to(ProjectWizardRegistryImpl.class).in(Singleton.class);
        install(new GinFactoryModuleBuilder().build(ProjectWizardFactory.class));
        bind(PreSelectedProjectTypeManager.class).to(PreSelectedProjectTypeManagerImpl.class).in(Singleton.class);
        bind(OAuth2AuthenticatorRegistry.class).to(OAuth2AuthenticatorRegistryImpl.class).in(Singleton.class);
    }

    private void configureImportWizard() {
        GinMultibinder.newSetBinder(binder(), ImportWizardRegistrar.class).addBinding().to(ZipImportWizardRegistrar.class);
        bind(ImportWizardRegistry.class).to(ImportWizardRegistryImpl.class).in(Singleton.class);
        install(new GinFactoryModuleBuilder().build(ImportWizardFactory.class));
        bind(ProjectNotificationSubscriber.class).to(ProjectNotificationSubscriberImpl.class);
    }

    /** Configure GWT-clients for Codenvy Platform API services */
    private void configurePlatformApiGwtClients() {
        bind(UserServiceClient.class).to(UserServiceClientImpl.class).in(Singleton.class);
        bind(UserProfileServiceClient.class).to(UserProfileServiceClientImpl.class).in(Singleton.class);
        bind(PreferencesServiceClient.class).to(PreferencesServiceClientImpl.class).in(Singleton.class);
        bind(GitServiceClient.class).to(GitServiceClientImpl.class).in(Singleton.class);
        bind(OAuthServiceClient.class).to(OAuthServiceClientImpl.class).in(Singleton.class);
        bind(FactoryServiceClient.class).to(FactoryServiceClientImpl.class).in(Singleton.class);
        bind(ProjectServiceClient.class).to(ProjectServiceClientImpl.class).in(Singleton.class);
        bind(WorkspaceServiceClient.class).to(WorkspaceServiceClientImpl.class).in(Singleton.class);
        bind(SshServiceClient.class).to(SshServiceClientImpl.class).in(Singleton.class);
        bind(ProjectImportersServiceClient.class).to(ProjectImportersServiceClientImpl.class).in(Singleton.class);
        bind(ProjectTypeServiceClient.class).to(ProjectTypeServiceClientImpl.class).in(Singleton.class);
        bind(ProjectTemplateServiceClient.class).to(ProjectTemplateServiceClientImpl.class).in(Singleton.class);
        bind(RecipeServiceClient.class).to(RecipeServiceClientImpl.class).in(Singleton.class);
        bind(MachineServiceClient.class).to(MachineServiceClientImpl.class).in(Singleton.class);
        bind(ProjectTypeRegistry.class).to(ProjectTypeRegistryImpl.class).in(Singleton.class);
        bind(ProjectTemplateRegistry.class).to(ProjectTemplateRegistryImpl.class).in(Singleton.class);
        bind(DebuggerServiceClient.class).to(DebuggerServiceClientImpl.class).in(Singleton.class);
    }

    /** API Bindings, binds API interfaces to the implementations */
    private void configureApiBinding() {
        // Agents
        bind(KeyBindingAgent.class).to(KeyBindingManager.class).in(Singleton.class);
        bind(SelectionAgent.class).to(SelectionAgentImpl.class).asEagerSingleton();
        bind(WorkspaceAgent.class).to(WorkspacePresenter.class).in(Singleton.class);
        bind(IconRegistry.class).to(IconRegistryImpl.class).in(Singleton.class);

        // UI Model
        bind(EditorMultiPartStack.class).to(EditorMultiPartStackPresenter.class).in(Singleton.class);
        bind(ActionManager.class).to(ActionManagerImpl.class).in(Singleton.class);

        GinMultibinder<NodeInterceptor> nodeInterceptors = GinMultibinder.newSetBinder(binder(), NodeInterceptor.class);
        nodeInterceptors.addBinding().to(DefaultNodeInterceptor.class);

        // Command API
        bind(CommandTypeRegistry.class).to(CommandTypeRegistryImpl.class).in(Singleton.class);

        bind(MacroRegistry.class).to(MacroRegistryImpl.class).in(Singleton.class);

        bind(MacroProcessor.class).to(MacroProcessorImpl.class).in(Singleton.class);

        GinMultibinder<Macro> macrosBinder = GinMultibinder.newSetBinder(binder(), Macro.class);
        macrosBinder.addBinding().to(EditorCurrentFileNameMacro.class);
        macrosBinder.addBinding().to(EditorCurrentFilePathMacro.class);
        macrosBinder.addBinding().to(EditorCurrentFileRelativePathMacro.class);
        macrosBinder.addBinding().to(EditorCurrentProjectNameMacro.class);
        macrosBinder.addBinding().to(EditorCurrentProjectTypeMacro.class);
        macrosBinder.addBinding().to(ExplorerCurrentFileNameMacro.class);
        macrosBinder.addBinding().to(ExplorerCurrentFilePathMacro.class);
        macrosBinder.addBinding().to(ExplorerCurrentFileParentPathMacro.class);
        macrosBinder.addBinding().to(ExplorerCurrentFileRelativePathMacro.class);
        macrosBinder.addBinding().to(ExplorerCurrentProjectNameMacro.class);
        macrosBinder.addBinding().to(ExplorerCurrentProjectTypeMacro.class);
        macrosBinder.addBinding().to(WorkspaceNameMacro.class);
    }

    /** Configure Core UI components, resources and views */
    protected void configureCoreUI() {
        GinMultibinder<PreferencePagePresenter> prefBinder = GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class);
        prefBinder.addBinding().to(AppearancePresenter.class);
        prefBinder.addBinding().to(ExtensionManagerPresenter.class);

        GinMultibinder<Theme> themeBinder = GinMultibinder.newSetBinder(binder(), Theme.class);
        themeBinder.addBinding().to(DarkTheme.class);
        themeBinder.addBinding().to(LightTheme.class);

        // Resources
        bind(PartStackUIResources.class).to(Resources.class).in(Singleton.class);
        // Views
        bind(WorkspaceView.class).to(WorkspaceViewImpl.class).in(Singleton.class);
        bind(PerspectiveView.class).to(PerspectiveViewImpl.class);
        bind(MainMenuView.class).to(MainMenuViewImpl.class).in(Singleton.class);
        bind(StatusPanelGroupView.class).to(StatusPanelGroupViewImpl.class).in(Singleton.class);

        bind(ToolbarView.class).to(ToolbarViewImpl.class);
        bind(ToolbarPresenter.class).annotatedWith(MainToolbar.class).to(ToolbarPresenter.class).in(Singleton.class);

        //configure drop down menu
        install(new GinFactoryModuleBuilder().implement(DropDownWidget.class, DropDownWidgetImpl.class)
                                             .build(DropDownListFactory.class));

        bind(NotificationManagerView.class).to(NotificationManagerViewImpl.class).in(Singleton.class);

        bind(EditorPartStackView.class);

        bind(EditorContentSynchronizer.class).to(EditorContentSynchronizerImpl.class).in(Singleton.class);
        install(new GinFactoryModuleBuilder().implement(EditorGroupSynchronization.class, EditorGroupSynchronizationImpl.class)
                                             .build(EditorGroupSychronizationFactory.class));

        bind(MessageDialogFooter.class);
        bind(MessageDialogView.class).to(MessageDialogViewImpl.class);
        bind(ConfirmDialogFooter.class);
        bind(ConfirmDialogView.class).to(ConfirmDialogViewImpl.class);
        bind(ChoiceDialogFooter.class);
        bind(ChoiceDialogView.class).to(ChoiceDialogViewImpl.class);
        bind(InputDialogFooter.class);
        bind(InputDialogView.class).to(InputDialogViewImpl.class);
        install(new GinFactoryModuleBuilder().implement(MessageDialog.class, MessageDialogPresenter.class)
                                             .implement(ConfirmDialog.class, ConfirmDialogPresenter.class)
                                             .implement(InputDialog.class, InputDialogPresenter.class)
                                             .implement(ChoiceDialog.class, ChoiceDialogPresenter.class)
                                             .build(DialogFactory.class));

        install(new GinFactoryModuleBuilder().implement(SubPanelView.class, SubPanelViewImpl.class)
                                             .build(SubPanelViewFactory.class));
        install(new GinFactoryModuleBuilder().implement(SubPanel.class, SubPanelPresenter.class)
                                             .build(SubPanelFactory.class));
        install(new GinFactoryModuleBuilder().implement(Tab.class, TabWidget.class)
                                             .build(TabItemFactory.class));

        install(new GinFactoryModuleBuilder().implement(ConsoleButton.class, ConsoleButtonImpl.class)
                                             .build(ConsoleButtonFactory.class));
        install(new GinFactoryModuleBuilder().implement(ProjectNotificationSubscriber.class,
                                                        ProjectNotificationSubscriberImpl.class)
                                             .build(ImportProjectNotificationSubscriberFactory.class));

        install(new GinFactoryModuleBuilder().build(FindResultNodeFactory.class));

        bind(UploadFileView.class).to(UploadFileViewImpl.class);
        bind(UploadFolderFromZipView.class).to(UploadFolderFromZipViewImpl.class);
        bind(PreferencesView.class).to(PreferencesViewImpl.class).in(Singleton.class);
        bind(NavigateToFileView.class).to(NavigateToFileViewImpl.class).in(Singleton.class);

        bind(ExtensionManagerView.class).to(ExtensionManagerViewImpl.class).in(Singleton.class);
        bind(AppearanceView.class).to(AppearanceViewImpl.class).in(Singleton.class);
        bind(FindActionView.class).to(FindActionViewImpl.class).in(Singleton.class);

        bind(HotKeysDialogView.class).to(HotKeysDialogViewImpl.class).in(Singleton.class);

        bind(RecentFileList.class).to(RecentFileStore.class).in(Singleton.class);

        install(new GinFactoryModuleBuilder().build(RecentFileActionFactory.class));

        install(new GinFactoryModuleBuilder().build(CommandProducerActionFactory.class));
    }

    /** Configures binding for Editor API */
    protected void configureEditorAPI() {
        bind(EditorAgent.class).to(EditorAgentImpl.class).in(Singleton.class);

        bind(EditorRegistry.class).to(EditorRegistryImpl.class).in(Singleton.class);
        bind(UserActivityManager.class).in(Singleton.class);
        install(new GinFactoryModuleBuilder().build(EditorTabContextMenuFactory.class));
    }

    /** Configure bindings for project's tree. */
    private void configureProjectTree() {
        bind(SettingsProvider.class).to(DummySettingsProvider.class).in(Singleton.class);
        bind(ProjectExplorerView.class).to(ProjectExplorerViewImpl.class).in(Singleton.class);
        bind(ProjectExplorerPart.class).to(ProjectExplorerPresenter.class).in(Singleton.class);

        GinMultibinder<NodeIconProvider> themeBinder = GinMultibinder.newSetBinder(binder(), NodeIconProvider.class);
        themeBinder.addBinding().to(FileIconProvider.class);
        themeBinder.addBinding().to(DockerfileIconProvider.class);
        bind(TreeResourceRevealer.class);
    }

    @Provides
    @Named("defaultFileType")
    @Singleton
    protected FileType provideDefaultFileType() {
        Resources res = GWT.create(Resources.class);
        return new FileType(res.defaultFile(), null);
    }

    @Provides
    @Singleton
    protected PartStackEventHandler providePartStackEventHandler(FocusManager partAgentPresenter) {
        return partAgentPresenter.getPartStackHandler();
    }

    @Provides
    @Singleton
    @Named("defaultPerspectiveId")
    protected String defaultPerspectiveId() {
        return PROJECT_PERSPECTIVE_ID;
    }

    @Provides
    @Singleton
    protected JsonFactory provideJsonFactory() {
        return Json.instance();
    }
}
