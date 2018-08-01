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
package org.eclipse.che.ide.ext.gwt.client.command;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.eclipse.che.ide.ext.gwt.client.GwtLocalizationConstants;

/**
 * The implementation of {@link GwtCommandPageView}.
 *
 * @author Artem Zatsarynnyi
 */
public class GwtCommandPageViewImpl implements GwtCommandPageView {

  private static final GwtPageViewImplUiBinder UI_BINDER =
      GWT.create(GwtPageViewImplUiBinder.class);

  private final FlowPanel rootElement;

  @UiField TextBox workingDirectory;
  @UiField TextBox gwtModule;
  @UiField TextBox codeServerAddress;

  @UiField(provided = true)
  GwtLocalizationConstants locale;

  private ActionDelegate delegate;

  @Inject
  public GwtCommandPageViewImpl(GwtLocalizationConstants locale) {
    this.locale = locale;

    rootElement = UI_BINDER.createAndBindUi(this);
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
  public String getWorkingDirectory() {
    return workingDirectory.getValue();
  }

  @Override
  public void setWorkingDirectory(String workingDirectory) {
    this.workingDirectory.setValue(workingDirectory);
  }

  @Override
  public String getGwtModule() {
    return gwtModule.getValue();
  }

  @Override
  public void setGwtModule(String gwtModule) {
    this.gwtModule.setValue(gwtModule);
  }

  @Override
  public String getCodeServerAddress() {
    return codeServerAddress.getValue();
  }

  @Override
  public void setCodeServerAddress(String codeServerAddress) {
    this.codeServerAddress.setValue(codeServerAddress);
  }

  @UiHandler({"workingDirectory"})
  void onWorkingDirectoryChanged(KeyUpEvent event) {
    // workingDirectory value may not be updated immediately after keyUp
    // therefore use the timer with delay=0
    new Timer() {
      @Override
      public void run() {
        delegate.onWorkingDirectoryChanged();
      }
    }.schedule(0);
  }

  @UiHandler({"gwtModule"})
  void onGwtModuleChanged(KeyUpEvent event) {
    // gwtModule value may not be updated immediately after keyUp
    // therefore use the timer with delay=0
    new Timer() {
      @Override
      public void run() {
        delegate.onGwtModuleChanged();
      }
    }.schedule(0);
  }

  @UiHandler({"codeServerAddress"})
  void onCodeServerAddressChanged(KeyUpEvent event) {
    // codeServerAddress value may not be updated immediately after keyUp
    // therefore use the timer with delay=0
    new Timer() {
      @Override
      public void run() {
        delegate.onCodeServerAddressChanged();
      }
    }.schedule(0);
  }

  interface GwtPageViewImplUiBinder extends UiBinder<FlowPanel, GwtCommandPageViewImpl> {}
}
