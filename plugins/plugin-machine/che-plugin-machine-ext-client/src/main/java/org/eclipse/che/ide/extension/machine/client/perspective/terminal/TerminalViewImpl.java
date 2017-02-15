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
package org.eclipse.che.ide.extension.machine.client.perspective.terminal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import javax.validation.constraints.NotNull;

/**
 * The class contains methods to display terminal.
 *
 * @author Dmitry Shnurenko
 */
final class TerminalViewImpl extends Composite implements TerminalView, RequiresResize {

    interface TerminalViewImplUiBinder extends UiBinder<Widget, TerminalViewImpl> {
    }

    private final static TerminalViewImplUiBinder UI_BINDER = GWT.create(TerminalViewImplUiBinder.class);

    @UiField
    FlowPanel terminalPanel;

    @UiField
    Label     unavailableLabel;

    private ActionDelegate delegate;

    private TerminalJso terminal;
    private Element terminalElement;

    public TerminalViewImpl() {
        initWidget(UI_BINDER.createAndBindUi(this));
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void openTerminal(@NotNull final TerminalJso terminal) {
        unavailableLabel.setVisible(false);

        this.terminal = terminal;
        terminalElement = terminalPanel.getElement();
        terminalPanel.setVisible(true);
        terminalElement.getStyle().setProperty("opacity", "0");

        terminal.open(terminalPanel.getElement());

        terminalElement.getFirstChildElement().getStyle().clearProperty("backgroundColor");
        terminalElement.getFirstChildElement().getStyle().clearProperty("color");
        terminalElement.getStyle().clearProperty("opacity");
    }

    /** {@inheritDoc} */
    @Override
    public void showErrorMessage(@NotNull String message) {
        unavailableLabel.setText(message);
        unavailableLabel.setVisible(true);

        terminalPanel.setVisible(false);
    }

    /**
     * Resize {@link TerminalJso} to current widget size.
     * To improve performance we should resize only visible terminals,
     * because "resize terminal" is quite expensive operation. When you
     * click on the tab to activate hidden terminal this method will be
     * executed too, so terminal will be resized anyway.
     */
    @Override
    public void onResize() {
        resizeTimer.cancel();

        if (terminalElement != null && isVisible()) {
            resizeTimer.schedule(200);
        }
    }

    private Timer resizeTimer = new Timer() {
        @Override
        public void run() {
            resizeTerminal();
        }
    };

    private void resizeTerminal() {
        TerminalGeometryJso geometryJso = terminal.proposeGeometry();
        int x = geometryJso.getCols();
        int y = geometryJso.getRows();
        if (x <= 0 || y <= 0) {
            resizeTimer.cancel();
            resizeTimer.schedule(500);
            return;
        }

        delegate.setTerminalSize(x, y);
    }
}
