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

package org.eclipse.che.ide.machine.chooser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;

import org.eclipse.che.api.core.model.machine.Machine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link MachineChooserView} which pops up list of the machines.
 * User can select machine with Enter key or cancel selection with Esc key.
 *
 * @author Artem Zatsarynnyi
 */
public class MachineChooserViewImpl extends PopupPanel implements MachineChooserView {

    private static final MachineChooserViewImplUiBinder UI_BINDER = GWT.create(MachineChooserViewImplUiBinder.class);

    /** Map that contains all shown machines. */
    private final Map<String, Machine> machinesById;

    @UiField
    DockLayoutPanel layoutPanel;

    @UiField
    ListBox machinesList;

    private ActionDelegate delegate;

    @Inject
    public MachineChooserViewImpl() {
        machinesById = new HashMap<>();

        setWidget(UI_BINDER.createAndBindUi(this));

        initView();
        addHandlers();
    }

    private void initView() {
        setAutoHideEnabled(true);
        setAnimationEnabled(true);
        setAnimationType(AnimationType.ROLL_DOWN);
    }

    private void addHandlers() {
        addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                if (event.isAutoClosed()) {
                    delegate.onCanceled();
                }
            }
        });

        machinesList.addDoubleClickHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                final String selectedMachineId = machinesList.getSelectedValue();

                if (selectedMachineId != null) {
                    final Machine selectedMachine = machinesById.get(selectedMachineId);

                    if (selectedMachine != null) {
                        delegate.onMachineSelected(selectedMachine);
                    }
                }
            }
        });

        machinesList.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                final int keyCode = event.getNativeEvent().getKeyCode();

                if (KeyCodes.KEY_ENTER == keyCode || KeyCodes.KEY_MAC_ENTER == keyCode) {
                    final String selectedMachineId = machinesList.getSelectedValue();

                    if (selectedMachineId != null) {
                        final Machine selectedMachine = machinesById.get(selectedMachineId);

                        if (selectedMachine != null) {
                            delegate.onMachineSelected(selectedMachine);
                        }
                    }
                }
            }
        });

        machinesList.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (KeyCodes.KEY_ESCAPE == event.getNativeKeyCode()) {
                    hide(true);
                }
            }
        });
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void show() {
        super.show();

        center();

        machinesList.setFocus(true);
    }

    @Override
    public void close() {
        hide();
    }

    @Override
    public void setMachines(List<? extends Machine> machines) {
        machinesList.clear();
        machinesById.clear();

        for (Machine machine : machines) {
            machinesById.put(machine.getId(), machine);

            machinesList.addItem(machine.getConfig().getName(), machine.getId());
        }

        machinesList.setVisibleItemCount(machines.size());
        machinesList.setSelectedIndex(0);

        // set height of the each row in the list to 15 px
        final int listHeight = 15 * machines.size();
        machinesList.setHeight(listHeight + "px");

        // set height of the entire panel
        layoutPanel.setHeight(20 + listHeight + "px");
    }

    interface MachineChooserViewImplUiBinder extends UiBinder<DockLayoutPanel, MachineChooserViewImpl> {
    }
}
