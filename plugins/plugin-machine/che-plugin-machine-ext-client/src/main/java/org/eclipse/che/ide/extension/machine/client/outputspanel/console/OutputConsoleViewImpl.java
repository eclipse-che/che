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
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.PreElement;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
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
 */
public class OutputConsoleViewImpl extends Composite implements OutputConsoleView {

    interface OutputConsoleViewUiBinder extends UiBinder<Widget, OutputConsoleViewImpl> {
    }

    private static final OutputConsoleViewUiBinder UI_BINDER   = GWT.create(OutputConsoleViewUiBinder.class);
    private static final int                       SCROLL_STEP = 3;

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
    private int     mouseDelta;
    private int     scrollLinesCount;
    private boolean scrolled;

    @Inject
    public OutputConsoleViewImpl() {
        initWidget(UI_BINDER.createAndBindUi(this));

        MouseWheelHandler mouseWheelHandler = new MouseWheelHandler() {
            @Override
            public void onMouseWheel(MouseWheelEvent event) {
                if (event.isNorth() && !scrolled) {
                    scrolled = true;

                    mouseDelta = scrollLinesCount;
                }

                mouseDelta += event.isSouth() ? SCROLL_STEP : -SCROLL_STEP;

                if (mouseDelta < 0) {
                    mouseDelta = 0;
                }
            }
        };

        consoleLines.addDomHandler(mouseWheelHandler, MouseWheelEvent.getType());
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
        if (!Strings.isNullOrEmpty(previewUrl)) {
            previewUrlLabel.setText(previewUrl);
            previewUrlLabel.setTitle(previewUrl);
            previewUrlLabel.setHref(previewUrl);
        } else {
            hidePreview();
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

        scrollBottom();
    }

    private void scrollBottom() {
        /** scroll bottom immediately if view is visible */
        if (mouseDelta >= scrollLinesCount) {
            scrollPanel.scrollToBottom();

            scrolled = false;

            mouseDelta = scrollLinesCount + SCROLL_STEP;
        }

        if (!carriageReturn) {
            scrollLinesCount++;
        }
    }
}