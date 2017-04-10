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
package org.eclipse.che.ide.extension.machine.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.actions.StopWorkspaceAction;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.extension.machine.client.actions.RunCommandAction;
import org.eclipse.che.ide.extension.machine.client.actions.ShowConsoleTreeAction;
import org.eclipse.che.ide.extension.machine.client.command.macros.ServerAddressMacroRegistrar;
import org.eclipse.che.ide.extension.machine.client.machine.MachineStatusHandler;
import org.eclipse.che.ide.extension.machine.client.perspective.terminal.TerminalInitializePromiseHolder;
import org.eclipse.che.ide.extension.machine.client.processes.NewTerminalAction;
import org.eclipse.che.ide.extension.machine.client.processes.actions.CloseConsoleAction;
import org.eclipse.che.ide.extension.machine.client.processes.actions.ReRunProcessAction;
import org.eclipse.che.ide.extension.machine.client.processes.actions.StopProcessAction;
import org.eclipse.che.ide.extension.machine.client.targets.EditTargetsAction;
import org.eclipse.che.lib.terminal.client.TerminalResources;
import org.eclipse.che.ide.util.input.KeyCodeMap;
import org.eclipse.che.plugin.requirejs.ide.RequireJsLoader;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_CONSOLES_TREE_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_RUN;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_WORKSPACE;
import static org.eclipse.che.ide.api.constraints.Constraints.FIRST;

/**
 * Machine extension entry point.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
@Singleton
@Extension(title = "Machine", version = "1.0.0")
public class MachineExtension {

    private final PerspectiveManager perspectiveManager;

    @Inject
    public MachineExtension(final MachineResources machineResources,
                            final TerminalResources terminalResources,
                            final EventBus eventBus,
                            final Provider<ServerAddressMacroRegistrar> machinePortProvider,
                            final PerspectiveManager perspectiveManager,
                            final Provider<MachineStatusHandler> machineStatusHandlerProvider,
                            final AppContext appContext,
                            final TerminalInitializePromiseHolder terminalModule,
                            final RequireJsLoader requireJsLoader) {
        this.perspectiveManager = perspectiveManager;

        machineResources.getCss().ensureInjected();
        terminalResources.getTerminalStyle().ensureInjected();
        machineStatusHandlerProvider.get();

        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                restoreTerminal();

                machinePortProvider.get();
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
            }
        });

        eventBus.addHandler(WorkspaceStartingEvent.TYPE, new WorkspaceStartingEvent.Handler() {
            @Override
            public void onWorkspaceStarting(WorkspaceStartingEvent event) {
                maximizeTerminal();
            }
        });

        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, new WorkspaceStoppedEvent.Handler() {
            @Override
            public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
                maximizeTerminal();
            }
        });

        if (appContext.getWorkspace() == null || WorkspaceStatus.RUNNING != appContext.getWorkspace().getStatus()) {
            maximizeTerminal();
        }

        Promise<Void> termInitPromise = AsyncPromiseHelper.createFromAsyncRequest(new AsyncPromiseHelper.RequestCall<Void>() {
            @Override
            public void makeCall(AsyncCallback<Void> callback) {
                injectTerminal(requireJsLoader, callback);
            }
        });
        terminalModule.setInitializerPromise(termInitPromise);
    }

    private void injectTerminal(RequireJsLoader rJsLoader, final AsyncCallback<Void> callback) {
        rJsLoader.require(new Callback<JavaScriptObject[], Throwable>() {
            @Override
            public void onFailure(Throwable reason) {
                callback.onFailure(reason);
            }

            @Override
            public void onSuccess(JavaScriptObject[] result) {
                callback.onSuccess(null);
            }
        }, new String[]{"term/xterm"}, new String[]{"Xterm"});
    }

    /**
     * Maximizes terminal.
     */
    private void maximizeTerminal() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                Perspective perspective = perspectiveManager.getActivePerspective();
                if (perspective != null) {
                    perspective.maximizeBottomPartStack();
                }
            }
        });
    }

    /**
     * Restores terminal to its default size.
     */
    private void restoreTerminal() {
        Perspective perspective = perspectiveManager.getActivePerspective();
        if (perspective != null) {
            perspective.restore();
        }
    }

    @Inject
    private void prepareActions(ActionManager actionManager,
                                KeyBindingAgent keyBinding,
                                StopWorkspaceAction stopWorkspaceAction,
                                RunCommandAction runCommandAction,
                                NewTerminalAction newTerminalAction,
                                EditTargetsAction editTargetsAction,
                                IconRegistry iconRegistry,
                                MachineResources machineResources,
                                ReRunProcessAction reRunProcessAction,
                                StopProcessAction stopProcessAction,
                                CloseConsoleAction closeConsoleAction,
                                ShowConsoleTreeAction showConsoleTreeAction) {
        final DefaultActionGroup workspaceMenu = (DefaultActionGroup)actionManager.getAction(GROUP_WORKSPACE);
        final DefaultActionGroup runMenu = (DefaultActionGroup)actionManager.getAction(GROUP_RUN);

        actionManager.registerAction("editTargets", editTargetsAction);

        actionManager.registerAction("stopWorkspace", stopWorkspaceAction);
        actionManager.registerAction("runCommand", runCommandAction);
        actionManager.registerAction("newTerminal", newTerminalAction);

        // add actions in main menu
        runMenu.add(newTerminalAction, FIRST);
        runMenu.addSeparator();
        runMenu.add(editTargetsAction);

        workspaceMenu.add(stopWorkspaceAction);

        // Consoles tree context menu group
        DefaultActionGroup consolesTreeContextMenu = (DefaultActionGroup)actionManager.getAction(GROUP_CONSOLES_TREE_CONTEXT_MENU);
        consolesTreeContextMenu.add(reRunProcessAction);
        consolesTreeContextMenu.add(stopProcessAction);
        consolesTreeContextMenu.add(closeConsoleAction);

        DefaultActionGroup partMenuGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_PART_MENU);
        partMenuGroup.add(showConsoleTreeAction);

        // Define hot-keys
        keyBinding.getGlobal().addKey(new KeyBuilder().alt().charCode(KeyCodeMap.F12).build(), "newTerminal");

        iconRegistry.registerIcon(new Icon("che.machine.icon", machineResources.devMachine()));
    }
}
