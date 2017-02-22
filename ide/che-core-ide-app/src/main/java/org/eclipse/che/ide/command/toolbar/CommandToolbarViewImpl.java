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
package org.eclipse.che.ide.command.toolbar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.command.toolbar.button.CommandPopupItem;
import org.eclipse.che.ide.command.toolbar.button.MachinePopupItem;
import org.eclipse.che.ide.ui.menubutton.MenuPopupButton;
import org.eclipse.che.ide.ui.menubutton.PopupActionHandler;
import org.eclipse.che.ide.ui.menubutton.PopupItem;
import org.eclipse.che.ide.ui.menubutton.PopupItemDataProvider;
import org.eclipse.che.ide.util.Pair;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/** Implementation of {@link CommandToolbarView}. */
@Singleton
public class CommandToolbarViewImpl implements CommandToolbarView {

    private static final CommandToolbarViewImplUiBinder UI_BINDER = GWT.create(CommandToolbarViewImplUiBinder.class);

    private final CommandResources resources;
    private final AppContext       appContext;

    @UiField
    FlowPanel   rootPanel;
    @UiField
    FlowPanel   buttonsPanel;
    @UiField
    SimplePanel processesListPanel;
    @UiField
    SimplePanel previewUrlListPanel;

    private MenuPopupButton          runCommandsButton;
    private RunPopupItemDataProvider runPopupItemDataProvider;

    private MenuPopupButton            debugCommandsButton;
    private DebugPopupItemDataProvider debugPopupItemDataProvider;

    private ActionDelegate          delegate;
    private List<ContextualCommand> runCommands;
    private List<ContextualCommand> debugCommands;
    private PopupItem               lastSelectedItem;

    @Inject
    public CommandToolbarViewImpl(CommandResources resources, AppContext appContext) {
        this.resources = resources;
        this.appContext = appContext;

        runCommands = new ArrayList<>();
        debugCommands = new ArrayList<>();

        UI_BINDER.createAndBindUi(this);

        setUpRunButton();
        setUpDebugButton();
    }

    private void setUpRunButton() {
        runPopupItemDataProvider = new RunPopupItemDataProvider();

        final SafeHtmlBuilder playIcon = new SafeHtmlBuilder();
        playIcon.appendHtmlConstant(FontAwesome.PLAY);

        runCommandsButton = new MenuPopupButton(playIcon.toSafeHtml(),
                                                runPopupItemDataProvider,
                                                new MyPopupActionHandler());

        runCommandsButton.setTitle("No command defined for Run. Configure it in commands panel.");

        runCommandsButton.asWidget().addStyleName(resources.commandToolbarCss().toolbarButton());

        buttonsPanel.add(runCommandsButton);
    }

    private void setUpDebugButton() {
        debugPopupItemDataProvider = new DebugPopupItemDataProvider();

        final SafeHtmlBuilder debugIcon = new SafeHtmlBuilder();
        debugIcon.appendHtmlConstant(FontAwesome.BUG);

        debugCommandsButton = new MenuPopupButton(debugIcon.toSafeHtml(),
                                                  debugPopupItemDataProvider,
                                                  new MyPopupActionHandler());

        debugCommandsButton.asWidget().addStyleName(resources.commandToolbarCss().toolbarButton());
        debugCommandsButton.asWidget().addStyleName(resources.commandToolbarCss().debugButton());

        buttonsPanel.add(debugCommandsButton);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

    @Override
    public void setRunCommands(List<ContextualCommand> commands) {
        runCommands.clear();
        runCommands.addAll(commands);

        runPopupItemDataProvider.handler.onItemDataChanged();

        updateRunButtonTooltip();
    }

    private void updateRunButtonTooltip() {
        if (runCommands.isEmpty()) {
            runCommandsButton.setTitle("No command defined for Run. Configure it in commands panel");
        } else {
            if (lastSelectedItem != null) {
                runCommandsButton.setTitle(lastSelectedItem.getName());
            } else {
                runCommandsButton.setTitle("Execute command of 'Run' goal");
            }
        }
    }

    @Override
    public void setDebugCommands(List<ContextualCommand> commands) {
        debugCommands.clear();
        debugCommands.addAll(commands);

        debugPopupItemDataProvider.handler.onItemDataChanged();

        updateDebugButtonTooltip();
    }

    private void updateDebugButtonTooltip() {
        if (debugCommands.isEmpty()) {
            debugCommandsButton.setTitle("No command defined for Debug. Configure it in commands panel");
        } else {
            if (lastSelectedItem != null) {
                debugCommandsButton.setTitle(lastSelectedItem.getName());
            } else {
                debugCommandsButton.setTitle("Execute command of 'Debug' goal");
            }
        }
    }

    @Override
    public AcceptsOneWidget getProcessesListContainer() {
        return processesListPanel;
    }

    @Override
    public AcceptsOneWidget getPreviewUrlsListContainer() {
        return previewUrlListPanel;
    }

    interface CommandToolbarViewImplUiBinder extends UiBinder<Widget, CommandToolbarViewImpl> {
    }

    private class RunPopupItemDataProvider implements PopupItemDataProvider {

        private ItemDataChangeHandler handler;

        @Override
        public PopupItem getDefaultItem() {
            if (lastSelectedItem != null) {
                return lastSelectedItem;
            }

            // TODO: return MachinePopupItem
            return new CommandPopupItem(runCommands.iterator().next());
        }

        @Override
        public List<PopupItem> getItems() {
            List<PopupItem> result = new ArrayList<>(runCommands.size());

            if (lastSelectedItem != null && lastSelectedItem instanceof MachinePopupItem) {
                result.add(new MachinePopupItem((MachinePopupItem)lastSelectedItem));
            }

            for (ContextualCommand command : runCommands) {
                result.add(new CommandPopupItem(command));
            }

            return result;
        }

        @Override
        public boolean isGroup(PopupItem popupItem) {
            if (popupItem instanceof CommandPopupItem) {
                return !appContext.getWorkspace().getRuntime().getMachines().isEmpty();
            }

            return false;
        }

        @Override
        public Pair<List<PopupItem>, String> getChildren(PopupItem parent) {
            List<PopupItem> result = new ArrayList<>();

            if (parent instanceof CommandPopupItem) {
                final ContextualCommand command = ((CommandPopupItem)parent).getCommand();

                for (Machine machine : appContext.getWorkspace().getRuntime().getMachines()) {
                    result.add(new MachinePopupItem(command, machine));
                }
            }

            return Pair.of(result, null);
        }

        @Override
        public void setItemDataChangedHandler(ItemDataChangeHandler handler) {
            this.handler = handler;
        }
    }

    private class DebugPopupItemDataProvider implements PopupItemDataProvider {

        private ItemDataChangeHandler handler;

        @Override
        public PopupItem getDefaultItem() {
            if (lastSelectedItem != null) {
                return lastSelectedItem;
            }

            // TODO: return MachinePopupItem
            return new CommandPopupItem(debugCommands.iterator().next());
        }

        @Override
        public List<PopupItem> getItems() {
            List<PopupItem> result = new ArrayList<>(debugCommands.size());

            if (lastSelectedItem != null && lastSelectedItem instanceof MachinePopupItem) {
                result.add(new MachinePopupItem((MachinePopupItem)lastSelectedItem));
            }

            for (ContextualCommand command : debugCommands) {
                result.add(new CommandPopupItem(command));
            }

            return result;
        }

        @Override
        public boolean isGroup(PopupItem popupItem) {
            if (popupItem instanceof CommandPopupItem) {
                return appContext.getWorkspace().getRuntime().getMachines().size() > 1;
            }

            return false;
        }

        @Override
        public Pair<List<PopupItem>, String> getChildren(PopupItem parent) {
            List<PopupItem> result = new ArrayList<>();

            if (parent instanceof CommandPopupItem) {
                final ContextualCommand command = ((CommandPopupItem)parent).getCommand();

                for (Machine machine : appContext.getWorkspace().getRuntime().getMachines()) {
                    result.add(new MachinePopupItem(command, machine));
                }
            }

            return Pair.of(result, null);
        }

        @Override
        public void setItemDataChangedHandler(ItemDataChangeHandler handler) {
            this.handler = handler;
        }
    }

    private class MyPopupActionHandler implements PopupActionHandler {

        @Override
        public void onItemSelected(PopupItem item) {
            if (item instanceof CommandPopupItem) {
                final ContextualCommand command = ((CommandPopupItem)item).getCommand();

                delegate.onCommandRun(command, null);
            } else if (item instanceof MachinePopupItem) {
                final MachinePopupItem machinePopupItem = (MachinePopupItem)item;

                delegate.onCommandRun(machinePopupItem.getCommand(), machinePopupItem.getMachine());
            }

            lastSelectedItem = item;
        }
    }
}
