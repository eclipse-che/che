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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.ui.dropdown.BaseListItem;
import org.eclipse.che.ide.ui.dropdown.DropDownListItemRenderer;

import static com.google.gwt.dom.client.Style.Float.RIGHT;
import static com.google.gwt.dom.client.Style.Unit.PX;

/** Renders widgets for representing a {@link Process}. */
class ProcessItemRenderer implements DropDownListItemRenderer {

    private static final CommandResources RESOURCES = GWT.create(CommandResources.class);

    private final BaseListItem<Process> item;
    private final StopProcessHandler    stopHandler;
    private final RerunProcessHandler   reRunHandler;

    private ProcessWidget headerWidget;
    private ProcessWidget listWidget;

    ProcessItemRenderer(BaseListItem<Process> listItem, StopProcessHandler stopProcessHandler, RerunProcessHandler reRunProcessHandler) {
        item = listItem;
        stopHandler = stopProcessHandler;
        reRunHandler = reRunProcessHandler;
    }

    @Override
    public Widget renderHeaderWidget() {
        if (headerWidget == null) {
            headerWidget = new ProcessWidget(item, stopHandler, reRunHandler);
        }

        return headerWidget;
    }

    @Override
    public Widget renderListWidget() {
        if (listWidget == null) {
            listWidget = new ProcessWidget(item, stopHandler, reRunHandler);
        }

        return listWidget;
    }

    /** Informs rendered widgets that related process has been stopped. */
    void notifyProcessStopped() {
        headerWidget.toggleStopped();
        listWidget.toggleStopped();
    }

    interface StopProcessHandler {
        /** Called when stopping {@code process} is requested. */
        void onStopProcess(Process process);
    }

    interface RerunProcessHandler {
        /** Called when rerunning {@code process} is requested. */
        void onRerunProcess(Process process);
    }

    /**
     * Widget for representing a {@link Process}.
     * Has different states for representing stopped and running processes.
     */
    private static class ProcessWidget extends FlowPanel {

        private final Label        pidLabel;
        private final ActionButton stopButton;
        private final ActionButton reRunButton;

        /** Stores true if widget displays stopped process and false for running process. */
        private boolean stopped;

        ProcessWidget(BaseListItem<Process> item, StopProcessHandler stopProcessHandler, RerunProcessHandler rerunProcessHandler) {
            super();

            final Process process = item.getValue();
            final Machine targetMachine = process.getMachine();
            stopped = !process.isAlive();

            final String labelText = targetMachine.getConfig().getName() + ": <b>" + process.getName() + "</b>";
            final Label nameLabel = new InlineHTML(labelText);
//            nameLabel.setWidth("230px");
//            nameLabel.getElement().getStyle().setFloat(LEFT);
            nameLabel.setTitle(process.getCommandLine());
            nameLabel.addStyleName(RESOURCES.commandToolbarCss().processesListItemNameLabel());

            pidLabel = new Label('#' + Integer.toString(process.getPid()));
            pidLabel.getElement().getStyle().setFloat(RIGHT);
            pidLabel.getElement().getStyle().setMarginRight(10, PX);

            final SafeHtmlBuilder safeHtmlBuilder1 = new SafeHtmlBuilder();
            safeHtmlBuilder1.appendHtmlConstant(FontAwesome.STOP);
            stopButton = new ActionButton(safeHtmlBuilder1.toSafeHtml());
            stopButton.addClickHandler(event -> stopProcessHandler.onStopProcess(process));

            final SafeHtmlBuilder safeHtmlBuilder2 = new SafeHtmlBuilder();
            safeHtmlBuilder2.appendHtmlConstant(FontAwesome.PLAY);
            reRunButton = new ActionButton(safeHtmlBuilder2.toSafeHtml());
            reRunButton.addClickHandler(event -> rerunProcessHandler.onRerunProcess(process));

            checkStopped();
            setHeight("25px");
            add(nameLabel);
            add(stopButton);
            add(reRunButton);
            add(pidLabel);
        }

        /** Toggle widget's state for displaying running or stopped process. */
        void toggleStopped() {
            stopped = !stopped;
            checkStopped();
        }

        /** Check whether widget displays stopped or running process and changes widget's state if it's required. */
        private void checkStopped() {
            pidLabel.setVisible(!stopped);
            reRunButton.setVisible(stopped);
            stopButton.setVisible(!stopped);
        }

        private class ActionButton extends ButtonBase {
            ActionButton(SafeHtml content) {
                super(Document.get().createDivElement());

                getElement().getStyle().setFloat(RIGHT);
                getElement().setInnerSafeHtml(content);
                asWidget().addStyleName(RESOURCES.commandToolbarCss().processesListItemActionButton());
            }
        }
    }
}
