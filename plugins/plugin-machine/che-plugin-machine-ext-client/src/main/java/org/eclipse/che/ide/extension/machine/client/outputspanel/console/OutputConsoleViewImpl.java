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
package org.eclipse.che.ide.extension.machine.client.outputspanel.console;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.PreElement;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * View representation of output console.
 *
 * @author Artem Zatsarynnyi
 * @author Vitaliy Guliy
 */
public class OutputConsoleViewImpl extends Composite implements OutputConsoleView, ScrollHandler {

    interface OutputConsoleViewUiBinder extends UiBinder<Widget, OutputConsoleViewImpl> {
    }

    private static final OutputConsoleViewUiBinder UI_BINDER   = GWT.create(OutputConsoleViewUiBinder.class);

    @UiField
    DockLayoutPanel consolePanel;

    @UiField
    FlowPanel       commandPanel;

    @UiField
    FlowPanel       previewPanel;

    @UiField
    Label           commandTitle;

    @UiField
    Label           commandLabel;

    @UiField
    ScrollPanel     scrollPanel;

    @UiField
    FlowPanel       consoleLines;

    @UiField
    Anchor          previewUrlLabel;

    /** If true - next printed line should replace the previous one. */
    private boolean carriageReturn;

    /** Follow the output. Scroll to the bottom automatically when <b>true</b>. */
    private boolean followOutput = true;

    /** Scroll to the bottom immediately when view become visible. */
    private boolean followScheduled = false;

    @Inject
    public OutputConsoleViewImpl() {
        initWidget(UI_BINDER.createAndBindUi(this));
        scrollPanel.addDomHandler(this, ScrollEvent.getType());
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
    }

    @Override
    public void hideCommand() {
        consolePanel.setWidgetHidden(commandPanel, true);
    }

    @Override
    public void hidePreview() {
        consolePanel.setWidgetHidden(previewPanel, true);
    }

    @Override
    public void printCommandLine(String commandLine) {
        commandLabel.setText(commandLine);
    }

    @Override
    public void printPreviewUrl(String previewUrl) {
        if (Strings.isNullOrEmpty(previewUrl)) {
            hidePreview();
        } else {
            previewUrlLabel.setText(previewUrl);
            previewUrlLabel.setTitle(previewUrl);
            previewUrlLabel.setHref(previewUrl);
        }
    }

    @Override
    public void print(String message, boolean cr) {
        if (carriageReturn) {
            Node lastChild = consoleLines.getElement().getLastChild();
            if (lastChild != null) {
                lastChild.removeFromParent();
            }
        }

        carriageReturn = cr;

        PreElement pre = DOM.createElement("pre").cast();
        pre.setInnerText(message.isEmpty() ? " " : message);
        consoleLines.getElement().appendChild(pre);

        followOutput();
    }

    @Override
    public void onScroll(ScrollEvent event) {
        // Do nothing if content height less scroll area height
        if (scrollPanel.getElement().getScrollHeight() < scrollPanel.getElement().getOffsetHeight()) {
            return;
        }

        // Follow output if scroll area is scrolled to the end
        if (scrollPanel.getElement().getScrollTop() + scrollPanel.getElement().getOffsetHeight() > scrollPanel.getElement().getScrollHeight()) {
            followOutput = true;
        } else {
            followOutput = false;
        }
    }

    /**
     * Scrolls to the bottom if following the output is enabled.
     */
    private void followOutput() {
        if (!followOutput) {
            return;
        }

        /** Scroll bottom immediately if view is visible */
        if (scrollPanel.getElement().getOffsetParent() != null) {
            scrollPanel.scrollToBottom();
            return;
        }

        /** Otherwise, check the visibility periodically and scroll the view when it's visible */
        if (!followScheduled) {
            followScheduled = true;

            Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
                @Override
                public boolean execute() {
                    if (!followOutput) {
                        followScheduled = false;
                        return false;
                    }

                    if (scrollPanel.getElement().getOffsetParent() != null) {
                        scrollPanel.scrollToBottom();
                        followScheduled = false;
                        return false;
                    }

                    return true;
                }
            }, 500);
        }
    }

}
