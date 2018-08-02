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
package org.eclipse.che.plugin.gdb.ide.configuration;

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
import java.util.Map;
import org.eclipse.che.ide.ui.listbox.CustomComboBox;

/**
 * The implementation of {@link GdbConfigurationPageView}.
 *
 * @author Artem Zatsarynnyi
 */
public class GdbConfigurationPageViewImpl implements GdbConfigurationPageView {

  private static final GDBConfigurationPageViewImplUiBinder UI_BINDER =
      GWT.create(GDBConfigurationPageViewImplUiBinder.class);

  private final FlowPanel rootElement;

  @UiField CheckBox devHost;
  @UiField CustomComboBox host;
  @UiField TextBox port;
  @UiField TextBox binaryPath;

  private ActionDelegate delegate;

  public GdbConfigurationPageViewImpl() {
    rootElement = UI_BINDER.createAndBindUi(this);
    devHost.addValueChangeHandler(
        new ValueChangeHandler<Boolean>() {
          @Override
          public void onValueChange(ValueChangeEvent<Boolean> event) {
            delegate.onDevHostChanged(event.getValue());
          }
        });
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
  public String getBinaryPath() {
    return binaryPath.getValue();
  }

  @Override
  public void setBinaryPath(String path) {
    this.binaryPath.setValue(path);
  }

  @Override
  public void setDevHost(boolean value) {
    devHost.setValue(value);
  }

  @Override
  public void setHostsList(Map<String, String> hosts) {
    host.clear();
    for (Map.Entry<String, String> entry : hosts.entrySet()) {
      host.addItem(entry.getValue(), entry.getKey());
    }
  }

  @Override
  public void setHostEnableState(boolean enable) {
    host.setEnabled(enable);
  }

  @Override
  public void setPortEnableState(boolean enable) {
    port.setEnabled(enable);
  }

  @UiHandler({"host"})
  void onHostKeyUp(KeyUpEvent event) {
    delegate.onHostChanged();
  }

  @UiHandler({"host"})
  void onHostChanged(ChangeEvent event) {
    delegate.onHostChanged();
  }

  @UiHandler({"port"})
  void onPortKeyUp(KeyUpEvent event) {
    delegate.onPortChanged();
  }

  @UiHandler({"binaryPath"})
  void onBinaryPathKeyUp(KeyUpEvent event) {
    delegate.onBinaryPathChanged();
  }

  interface GDBConfigurationPageViewImplUiBinder
      extends UiBinder<FlowPanel, GdbConfigurationPageViewImpl> {}
}
