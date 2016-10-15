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
package org.eclipse.che.ide.extension.machine.client.perspective.terminal.container;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.extension.machine.client.inject.factories.TerminalFactory;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.extension.machine.client.perspective.terminal.TerminalPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;

import java.util.HashMap;
import java.util.Map;

/**
 * The class contains business logic which allows show needed terminal depending on current machine.
 *
 * @author Dmitry Shnurenko
 */
public class TerminalContainer implements TabPresenter, MachineStateEvent.Handler {

    private final TerminalContainerView          view;
    private final TerminalFactory                terminalFactory;
    private final Map<String, TerminalPresenter> terminals;

    @Inject
    public TerminalContainer(TerminalContainerView view, TerminalFactory terminalFactory) {
        this.view = view;
        this.terminalFactory = terminalFactory;

        this.terminals = new HashMap<>();
    }

    /**
     * Adds terminal for current machine. Or if terminal already exist updates terminal.
     *
     * @param machine
     *         machine for which terminal will be added or updated
     */
    public void addOrShowTerminal(MachineEntity machine) {
        String machineId = machine.getId();

        TerminalPresenter terminal = terminals.get(machineId);
        if (terminal != null) {
            terminal.connect();
            view.showTerminal(terminal);
            return;
        }

        TerminalPresenter newTerminal = terminalFactory.create(machine);

        terminals.put(machineId, newTerminal);

        view.addTerminal(newTerminal);
    }

    /** {@inheritDoc} */
    @Override
    public IsWidget getView() {
        return view;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public void onMachineCreating(MachineStateEvent event) {
    }

    /** {@inheritDoc} */
    @Override
    public void onMachineRunning(MachineStateEvent event) {
        //to do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        String destroyedMachineId = event.getMachineId();

        terminals.remove(destroyedMachineId);
    }
}
