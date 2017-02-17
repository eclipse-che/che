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

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Timer;
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

/** Renders widgets for representing a {@link Process}. */
class ProcessItemRenderer implements DropDownListItemRenderer {

    private static final CommandResources RESOURCES = GWT.create(CommandResources.class);

    private final BaseListItem<Process> item;
    private final StopProcessHandler    stopHandler;
    private final RerunProcessHandler   reRunHandler;

    private ProcessWidget headerWidget;
    private ProcessWidget listWidget;

    ProcessItemRenderer(BaseListItem<Process> listItem,
                        StopProcessHandler stopProcessHandler,
                        RerunProcessHandler reRunProcessHandler) {
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
        private final Label        durationLabel;
        private final ActionButton stopButton;
        private final ActionButton reRunButton;
        private final Timer        updateDurationTimer;

        /** Stores true if widget displays stopped process and false for running process. */
        private boolean stopped;

        ProcessWidget(BaseListItem<Process> item, StopProcessHandler stopProcessHandler, RerunProcessHandler rerunProcessHandler) {
            super();

            addStyleName(RESOURCES.commandToolbarCss().processWidget());

            final Process process = item.getValue();
            final Machine targetMachine = process.getMachine();
            stopped = !process.isAlive();

            final String labelText = targetMachine.getConfig().getName() + ": <b>" + process.getName() + "</b>";
            final Label nameLabel = new InlineHTML(labelText);
            nameLabel.setTitle(process.getCommandLine());
            nameLabel.addStyleName(RESOURCES.commandToolbarCss().processWidgetText());
            nameLabel.addStyleName(RESOURCES.commandToolbarCss().processWidgetNameLabel());

            durationLabel = new Label();
            durationLabel.addStyleName(RESOURCES.commandToolbarCss().processWidgetText());
            durationLabel.addStyleName(RESOURCES.commandToolbarCss().processWidgetPidLabel());

            updateDurationTimer = new Timer() {
                final Duration duration = new Duration();

                @Override
                public void run() {
                    final int elapsedSec = duration.elapsedMillis() / 1000;
                    final int minutesPart = elapsedSec / 60;
                    final int secondsPart = elapsedSec - minutesPart * 60;

                    durationLabel.setText((minutesPart < 10 ? "0" + minutesPart : minutesPart) + ":" +
                                          (secondsPart < 10 ? "0" + secondsPart : secondsPart));
                }

                @Override
                public void cancel() {
                    super.cancel();

                    durationLabel.setVisible(false);
                }
            };
            updateDurationTimer.scheduleRepeating(1000);

            pidLabel = new Label('#' + Integer.toString(process.getPid()));
            pidLabel.addStyleName(RESOURCES.commandToolbarCss().processWidgetText());
            pidLabel.addStyleName(RESOURCES.commandToolbarCss().processWidgetPidLabel());

            final SafeHtmlBuilder safeHtmlBuilder1 = new SafeHtmlBuilder();
            safeHtmlBuilder1.appendHtmlConstant(FontAwesome.STOP);
            stopButton = new ActionButton(safeHtmlBuilder1.toSafeHtml());
            stopButton.addClickHandler(event -> stopProcessHandler.onStopProcess(process));

            final SafeHtmlBuilder safeHtmlBuilder2 = new SafeHtmlBuilder();
            safeHtmlBuilder2.appendHtmlConstant(FontAwesome.PLAY);
            reRunButton = new ActionButton(safeHtmlBuilder2.toSafeHtml());
            reRunButton.addClickHandler(event -> rerunProcessHandler.onRerunProcess(process));

            checkStopped();
            add(nameLabel);
            add(stopButton);
            add(reRunButton);
            add(durationLabel);
            add(pidLabel);
        }

        /** Toggle widget's state for displaying running or stopped process. */
        void toggleStopped() {
            stopped = !stopped;
            checkStopped();

            updateDurationTimer.cancel();
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
                asWidget().addStyleName(RESOURCES.commandToolbarCss().processWidgetActionButton());
            }
        }
    }
}
