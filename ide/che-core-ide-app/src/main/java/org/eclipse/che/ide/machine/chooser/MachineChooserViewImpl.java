/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.che.ide.api.workspace.model.MachineImpl;

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
  private final Map<String, MachineImpl> machinesById;

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
            final MachineImpl selectedMachine = machinesById.get(selectedMachineId);

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
              final MachineImpl selectedMachine = machinesById.get(selectedMachineId);

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
  public void setMachines(List<? extends MachineImpl> machines) {
    machinesList.clear();
    machinesById.clear();

    machines.forEach(
        machine -> {
          machinesById.put(machine.getName(), machine);
          machinesList.addItem(machine.getName());
        });

    machinesList.setVisibleItemCount(machines.size());
    machinesList.setSelectedIndex(0);
  }

  interface MachineChooserViewImplUiBinder extends UiBinder<FlowPanel, MachineChooserViewImpl> {}
}
