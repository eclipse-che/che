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
package org.eclipse.che.ide.command.toolbar.processes;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.command.CommandAddedEvent;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandRemovedEvent;
import org.eclipse.che.ide.api.command.CommandsLoadedEvent;
import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.command.toolbar.CommandCreationGuide;
import org.eclipse.che.ide.command.toolbar.ToolbarMessages;

/**
 * Empty state widget for processes dropdown list. Has two states:
 * <ul>
 * <li>there is no command;</li>
 * <li>there is no process.</li>
 * </ul>
 *
 * @see org.eclipse.che.ide.ui.dropdown.DropdownList#DropdownList(Widget)
 */
@Singleton
public class EmptyListWidget extends FlowPanel {

    private final CommandManager       commandManager;
    private final CommandCreationGuide commandCreationGuide;

    private FlowPanel noProcessWidget;
    private FlowPanel noCommandWidget;

    @Inject
    public EmptyListWidget(CommandManager commandManager, CommandCreationGuide commandCreationGuide, EventBus eventBus) {
        this.commandManager = commandManager;
        this.commandCreationGuide = commandCreationGuide;

        eventBus.addHandler(CommandsLoadedEvent.getType(), e -> updateState());
        eventBus.addHandler(CommandAddedEvent.getType(), e -> updateState());
        eventBus.addHandler(CommandRemovedEvent.getType(), e -> updateState());
    }

    @Inject
    private void init(CommandResources resources, ToolbarMessages messages) {
        // initialize widget for the state when there's no process
        final Label commandNameLabel = new InlineHTML("Ready");
        commandNameLabel.addStyleName(resources.commandToolbarCss().processWidgetText());
        commandNameLabel.addStyleName(resources.commandToolbarCss().processWidgetCommandNameLabel());

        final Label machineNameLabel = new InlineHTML("&nbsp; - start command");
        machineNameLabel.addStyleName(resources.commandToolbarCss().processWidgetText());
        machineNameLabel.addStyleName(resources.commandToolbarCss().processWidgetMachineNameLabel());

        noProcessWidget = new FlowPanel();
        noProcessWidget.addStyleName(resources.commandToolbarCss().processWidgetText());
        noProcessWidget.add(commandNameLabel);
        noProcessWidget.add(machineNameLabel);


        // initialize widget for the state when there's no command
        final Label hintLabel = new InlineHTML(messages.guideItemLabel());
        hintLabel.addStyleName(resources.commandToolbarCss().processWidgetText());
        hintLabel.addStyleName(resources.commandToolbarCss().processWidgetCommandNameLabel());
        hintLabel.addClickHandler(event -> commandCreationGuide.guide());

        noCommandWidget = new FlowPanel();
        noCommandWidget.addStyleName(resources.commandToolbarCss().processWidgetText());
        noCommandWidget.add(hintLabel);
    }

    private void updateState() {
        if (commandManager.getCommands().isEmpty()) {
            showNoCommandWidget();
        } else {
            showNoProcessWidget();
        }
    }

    private void showNoCommandWidget() {
        noProcessWidget.removeFromParent();
        add(noCommandWidget);
    }

    private void showNoProcessWidget() {
        noCommandWidget.removeFromParent();
        add(noProcessWidget);
    }
}
