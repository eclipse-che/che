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

import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
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

/**
 *
 */
@Singleton
public class CommandToolbarViewImpl implements CommandToolbarView {

    private final AppContext appContext;

    private MenuPopupButton          runCommandsButton;
    private RunPopupItemDataProvider runPopupItemDataProvider;
    private MenuPopupButton          debugCommandsButton;
    private ActionDelegate           delegate;
    private List<ContextualCommand>  commands;
    private PopupItem                lastSelectedItem;
    private FlowPanel                rootPanel;

    @Inject
    public CommandToolbarViewImpl(CommandResources resources, AppContext appContext) {
        this.appContext = appContext;

        rootPanel = new FlowPanel();
        rootPanel.getElement().getStyle().setFloat(Style.Float.LEFT);

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
        runCommandsButton.asWidget().addStyleName(resources.commandToolbarCss().runButton());

        rootPanel.add(runCommandsButton);
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

        rootPanel.add(debugCommandsButton);
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
        return new AcceptsOneWidget() {
            @Override
            public void setWidget(IsWidget w) {
                rootPanel.add(w);
                w.asWidget().getElement().getStyle().setFloat(Style.Float.LEFT);
            }
        };
    }

    @Override
    public AcceptsOneWidget getPreviewUrlsListContainer() {
        return new AcceptsOneWidget() {
            @Override
            public void setWidget(IsWidget w) {
                rootPanel.add(w);
            }
        };
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
