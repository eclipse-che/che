/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.jdb.ide.configuration;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ui.listbox.CustomComboBox;
import org.eclipse.che.ide.util.Pair;

/**
 * The implementation of {@link JavaDebugConfigurationPageView}.
 *
 * @author Artem Zatsarynnyi
 */
public class JavaDebugConfigurationPageViewImpl implements JavaDebugConfigurationPageView {

  private static final JavaDebugConfigurationPageViewImplUiBinder UI_BINDER =
      GWT.create(JavaDebugConfigurationPageViewImplUiBinder.class);

  private final FlowPanel rootElement;

  @UiField CheckBox devHost;
  @UiField TextBox host;
  @UiField CustomComboBox port;

  private ActionDelegate delegate;
  private List<Pair<String, String>> ports;

  public JavaDebugConfigurationPageViewImpl() {
    rootElement = UI_BINDER.createAndBindUi(this);
    ports = new ArrayList<>();

    devHost.addValueChangeHandler(
        new ValueChangeHandler<Boolean>() {
          @Override
          public void onValueChange(ValueChangeEvent<Boolean> event) {
            delegate.onDevHostChanged(event.getValue());
          }
        });

    updateDialog();
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Widget asWidget() {
    return rootElement;
  }

  @Override
  public String getHost() {
    return host.getValue();
  }

  @Override
  public void setHost(String host) {
    this.host.setValue(host);
  }

  @Override
  public int getPort() {
    String port = this.port.getValue().trim();
    if (port.isEmpty()) {
      return 0;
    }

    try {
      return Integer.valueOf(port);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  @Override
  public void setPort(int port) {
    this.port.setValue(port <= 0 ? "" : String.valueOf(port));
  }

  @Override
  public void setHostEnableState(boolean enable) {
    host.setEnabled(enable);
  }

  @Override
  public void setPortsList(@NotNull List<Pair<String, String>> ports) {
    this.ports = ports;
    updatePortsList();
  }

  @Override
  public void setDevHost(boolean value) {
    devHost.setValue(value, true);
  }

  private void updatePortsList() {
    port.clear();
    for (Pair<String, String> entry : ports) {
      port.addItem(entry.first, entry.second);
    }
  }

  private void updateDialog() {
    boolean connectToDevMachine = devHost.getValue();
    if (connectToDevMachine) {
      updatePortsList();
      port.setFocus(true);
    } else {
      host.selectAll();
      host.setFocus(true);
      port.clear();
    }
  }

  @UiHandler({"host"})
  void onHostKeyUp(KeyUpEvent event) {
    delegate.onHostChanged();
  }

  @UiHandler({"port"})
  void onPortKeyUp(KeyUpEvent event) {
    delegate.onPortChanged();
  }

  @UiHandler({"port"})
  void onPortChanged(ChangeEvent event) {
    delegate.onPortChanged();
  }

  interface JavaDebugConfigurationPageViewImplUiBinder
      extends UiBinder<FlowPanel, JavaDebugConfigurationPageViewImpl> {}
}
