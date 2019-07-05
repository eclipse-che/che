/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.ide.configuration;

import com.google.gwt.core.client.GWT;
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

/**
 * The implementation of {@link ZendDbgConfigurationPageView}.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgConfigurationPageViewImpl implements ZendDbgConfigurationPageView {

  private static final ZendDebugConfigurationPageViewImplUiBinder UI_BINDER =
      GWT.create(ZendDebugConfigurationPageViewImplUiBinder.class);

  private final FlowPanel rootElement;
  private ActionDelegate delegate;
  @UiField TextBox clientHostIP;
  @UiField TextBox debugPort;
  @UiField CheckBox breakAtFirstLine;
  @UiField CheckBox useSslEncryption;

  public ZendDbgConfigurationPageViewImpl() {
    rootElement = UI_BINDER.createAndBindUi(this);
    breakAtFirstLine.addValueChangeHandler(
        new ValueChangeHandler<Boolean>() {
          @Override
          public void onValueChange(ValueChangeEvent<Boolean> event) {
            delegate.onBreakAtFirstLineChanged(event.getValue());
          }
        });
    useSslEncryption.addValueChangeHandler(
        new ValueChangeHandler<Boolean>() {
          @Override
          public void onValueChange(ValueChangeEvent<Boolean> event) {
            delegate.onUseSslEncryptionChanged(event.getValue());
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
  public boolean getBreakAtFirstLine() {
    return breakAtFirstLine.getValue();
  }

  @Override
  public void setBreakAtFirstLine(boolean value) {
    this.breakAtFirstLine.setValue(value);
  }

  @Override
  public String getClientHostIP() {
    return clientHostIP.getValue();
  }

  @Override
  public void setClientHostIP(String value) {
    this.clientHostIP.setValue(value);
  }

  @Override
  public int getDebugPort() {
    String port = debugPort.getValue().trim();
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
  public void setDebugPort(int value) {
    this.debugPort.setValue(value <= 0 ? "" : String.valueOf(value));
  }

  @Override
  public boolean getUseSslEncryption() {
    return useSslEncryption.getValue();
  }

  @Override
  public void setUseSslEncryption(boolean value) {
    this.useSslEncryption.setValue(value);
  }

  private void updateDialog() {
    clientHostIP.setFocus(true);
  }

  @UiHandler({"clientHostIP"})
  void onClientHostIPChanged(KeyUpEvent event) {
    delegate.onClientHostIPChanged();
  }

  @UiHandler({"debugPort"})
  void onDebugPortChanged(KeyUpEvent event) {
    delegate.onDebugPortChanged();
  }

  interface ZendDebugConfigurationPageViewImplUiBinder
      extends UiBinder<FlowPanel, ZendDbgConfigurationPageViewImpl> {}
}
