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
import org.eclipse.che.ide.command.toolbar.button.MenuPopupButton;
import org.eclipse.che.ide.command.toolbar.button.PopupActionHandler;
import org.eclipse.che.ide.command.toolbar.button.PopupItem;
import org.eclipse.che.ide.command.toolbar.button.PopupItemDataProvider;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/** Implementation of {@link CommandToolbarView}. */
@Singleton
public class CommandToolbarViewImpl implements CommandToolbarView {

    private static final CommandToolbarViewImplUiBinder UI_BINDER = GWT.create(CommandToolbarViewImplUiBinder.class);

    private final AppContext appContext;

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
    private MenuPopupButton          debugCommandsButton;
    private ActionDelegate           delegate;
    private List<ContextualCommand>  commands;
    private PopupItem                lastSelectedItem;

    @Inject
    public CommandToolbarViewImpl(CommandResources resources, AppContext appContext) {
        this.appContext = appContext;

        UI_BINDER.createAndBindUi(this);

        setUpRunButton(resources);
        setUpDebugButton(resources);
    }

    private void setUpRunButton(CommandResources resources) {
        runPopupItemDataProvider = new RunPopupItemDataProvider();

        final SafeHtmlBuilder playIcon = new SafeHtmlBuilder();
        playIcon.appendHtmlConstant(FontAwesome.PLAY);

        runCommandsButton = new MenuPopupButton(playIcon.toSafeHtml(), runPopupItemDataProvider, new PopupActionHandler() {
            @Override
            public void onItemSelected(PopupItem item) {
                if (item instanceof CommandPopupItem) {
                    Log.error(CommandToolbarView.class, "Item " + item + " should be MachinePopupItem");
                } else if (item instanceof MachinePopupItem) {
                    MachinePopupItem machinePopupItem = (MachinePopupItem)item;
                    delegate.onCommandRun(machinePopupItem.getCommand(), machinePopupItem.getMachine());
                }

                lastSelectedItem = item;

            }
        });

        runCommandsButton.asWidget().addStyleName(resources.commandToolbarCss().toolbarButton());

        buttonsPanel.add(runCommandsButton);
    }

    private void setUpDebugButton(CommandResources resources) {
        final SafeHtmlBuilder debugIcon = new SafeHtmlBuilder();
        debugIcon.appendHtmlConstant(FontAwesome.BUG);

        debugCommandsButton = new MenuPopupButton(debugIcon.toSafeHtml(), new PopupItemDataProvider() {
            private ItemDataChangeHandler handler;

            @Override
            public PopupItem getDefaultItem() {
                return null;
            }

            @Override
            public List<PopupItem> getItems() {
                return null;
            }

            @Override
            public boolean isGroup(PopupItem popupItem) {
                return false;
            }

            @Override
            public Pair<List<PopupItem>, String> getChildren(PopupItem parent) {
                return null;
            }

            @Override
            public void setItemDataChangedHandler(ItemDataChangeHandler handler) {
                this.handler = handler;
            }
        }, new PopupActionHandler() {
            @Override
            public void onItemSelected(PopupItem item) {

            }
        });

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
        this.commands = commands;
        runPopupItemDataProvider.handler.onItemDataChanged();
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
            return new CommandPopupItem(commands.iterator().next());
        }

        @Override
        public List<PopupItem> getItems() {
            List<PopupItem> result = new ArrayList<>(commands.size());

            if (lastSelectedItem != null && lastSelectedItem instanceof MachinePopupItem) {
                result.add(new MachinePopupItem((MachinePopupItem)lastSelectedItem));
            }

            for (ContextualCommand command : commands) {
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
}
