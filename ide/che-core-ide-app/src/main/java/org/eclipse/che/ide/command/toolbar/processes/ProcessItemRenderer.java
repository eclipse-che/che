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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.ui.dropdown.AbstractListItemRenderer;
import org.eclipse.che.ide.ui.dropdown.BaseListItem;

import java.util.LinkedList;
import java.util.List;

import static com.google.gwt.dom.client.Style.Float.LEFT;
import static com.google.gwt.dom.client.Style.Float.RIGHT;

/**
 * Renders widgets for representing a {@link Process}.
 */
class ProcessItemRenderer extends AbstractListItemRenderer<BaseListItem<Process>> {

    /** List of all rendered widgets. */
    private final List<ProcessWidget> widgets;
    private final StopProcessHandler  stopHandler;
    private final RerunProcessHandler reRunHandler;

    ProcessItemRenderer(BaseListItem<Process> listItem,
                        StopProcessHandler stopProcessHandler,
                        RerunProcessHandler reRunProcessHandler) {
        super(listItem);

        widgets = new LinkedList<>();
        stopHandler = stopProcessHandler;
        reRunHandler = reRunProcessHandler;
    }

    /**
     * {@inheritDoc}
     * <p>Rendered widget depends on the given {@code process}'s aliveness state.
     */
    @Override
    public Widget getWidget() {
        final ProcessWidget widget = new ProcessWidget(item, stopHandler, reRunHandler);
        widgets.add(widget);

        return widget;
    }

    /** Informs all rendered widgets that related process has been stopped. */
    void notifyProcessStopped() {
        widgets.forEach(ProcessWidget::setStopped);
    }

    interface StopProcessHandler {
        /** Called when stopping {@code process} is requested. */
        void onStopProcess(Process process);
    }

    interface RerunProcessHandler {
        /** Called when rerunning {@code process} is requested. */
        void onRerunProcess(Process process);
    }

    private static class ProcessWidget extends FlowPanel {

        private final Label        pidLabel;
        private final ActionButton button;

        private boolean stopped;

        ProcessWidget(BaseListItem<Process> item, StopProcessHandler stopProcessHandler, RerunProcessHandler rerunProcessHandler) {
            super();

            setHeight("25px");

            final Process process = item.getValue();

            stopped = !process.isAlive();

            final String labelText = process.getMachine().getConfig().getName() + ": <b>" + process.getName() + "</b>";
            final Label nameLabel = new InlineHTML(labelText);
            nameLabel.setWidth("230px");
            nameLabel.getElement().getStyle().setFloat(LEFT);
            nameLabel.setTitle(process.getCommandLine());

            pidLabel = new Label('#' + Integer.toString(process.getPid()));
            pidLabel.getElement().getStyle().setFloat(RIGHT);

            button = new ActionButton();
            button.addDomHandler(event -> {
                if (stopped) {
                    rerunProcessHandler.onRerunProcess(process);
                } else {
                    stopProcessHandler.onStopProcess(process);
                }
            }, ClickEvent.getType());

            add(nameLabel);
            add(button);
            add(pidLabel);
        }

        /**
         * Changes widget for representing process as stopped.
         * Does nothing in case widget is already represents stopped process.
         */
        void setStopped() {
            if (!stopped) {
                stopped = true;
                pidLabel.removeFromParent();
                button.setHTML("re-run");
            }
        }

        private static class ActionButton extends Button {
            ActionButton() {
                super();
                getElement().getStyle().setFloat(RIGHT);
                setHTML("stop");
            }
        }
    }
}
