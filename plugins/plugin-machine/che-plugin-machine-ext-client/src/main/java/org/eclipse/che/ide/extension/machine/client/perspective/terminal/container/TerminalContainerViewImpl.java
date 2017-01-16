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
package org.eclipse.che.ide.extension.machine.client.perspective.terminal.container;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.extension.machine.client.perspective.terminal.TerminalPresenter;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * The class contains business logic which allows control terminals. The class is container which contains all terminals for all machines.
 *
 * @author Dmitry Shnurenko
 */
final class TerminalContainerViewImpl extends Composite implements TerminalContainerView, RequiresResize {
    private static TerminalContainerViewImplUiBinder ourUiBinder = GWT.create(TerminalContainerViewImplUiBinder.class);
    private final List<TerminalPresenter> terminals;
    @UiField
    FlowPanel container;

    public TerminalContainerViewImpl() {
        initWidget(ourUiBinder.createAndBindUi(this));

        this.terminals = new ArrayList<>();
    }

    @Override
    public void onResize() {
        for (int i = 0; i < container.getWidgetCount(); i++) {
            Widget widget = container.getWidget(i);
            if(widget instanceof RequiresResize){
                ((RequiresResize)widget).onResize();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addTerminal(@NotNull TerminalPresenter terminal) {
        terminals.add(terminal);

        container.add(terminal.getView());
    }

    /** {@inheritDoc} */
    @Override
    public void showTerminal(@NotNull TerminalPresenter terminal) {
        for (TerminalPresenter presenter : terminals) {
            presenter.setVisible(false);
        }

        terminal.setVisible(true);
        onResize();
    }

    interface TerminalContainerViewImplUiBinder extends UiBinder<Widget, TerminalContainerViewImpl> {
    }
}