/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.machine.chooser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.machine.Machine;

/**
 * Implementation of {@link MachineChooserView} which pops up list of the machines. User can select
 * machine with Enter key or cancel selection with Esc key.
 *
 * @author Artem Zatsarynnyi
 */
public class MachineChooserViewImpl extends PopupPanel implements MachineChooserView {

  private static final MachineChooserViewImplUiBinder UI_BINDER =
      GWT.create(MachineChooserViewImplUiBinder.class);

  /** Map that contains all shown machines. */
  private final Map<String, Machine> machinesById;

  @UiField ListBox machinesList;

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
    addCloseHandler(
        event -> {
          if (event.isAutoClosed()) {
            delegate.onCanceled();
          }
        });

    machinesList.addDoubleClickHandler(
        event -> {
          final String selectedMachineId = machinesList.getSelectedValue();

          if (selectedMachineId != null) {
            final Machine selectedMachine = machinesById.get(selectedMachineId);

            if (selectedMachine != null) {
              delegate.onMachineSelected(selectedMachine);
            }
          }
        });

    machinesList.addKeyPressHandler(
        event -> {
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
        });

    machinesList.addKeyDownHandler(
        event -> {
          if (KeyCodes.KEY_ESCAPE == event.getNativeKeyCode()) {
            hide(true);
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

    machines.forEach(
        machine -> {
          machinesById.put(machine.getId(), machine);
          machinesList.addItem(machine.getConfig().getName(), machine.getId());
        });

    machinesList.setVisibleItemCount(machines.size());
    machinesList.setSelectedIndex(0);
  }

  interface MachineChooserViewImplUiBinder extends UiBinder<FlowPanel, MachineChooserViewImpl> {}
}
