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
package org.eclipse.che.ide.command.type.custom;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * The implementation of {@link CustomPageView}.
 *
 * @author Artem Zatsarynnyi
 */
public class CustomPageViewImpl implements CustomPageView {

  private static final CustomPageViewImplUiBinder UI_BINDER =
      GWT.create(CustomPageViewImplUiBinder.class);

  private final FlowPanel rootElement;

  @UiField TextArea commandLine;

  private ActionDelegate delegate;

  public CustomPageViewImpl() {
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
  public String getCommandLine() {
    return commandLine.getValue();
  }

  @Override
  public void setCommandLine(String commandLine) {
    this.commandLine.setValue(commandLine);
  }

  @UiHandler({"commandLine"})
  void onKeyUp(KeyUpEvent event) {
    // commandLine value may not be updated immediately after keyUp
    // therefore use the timer with zero delay
    new Timer() {
      @Override
      public void run() {
        delegate.onCommandLineChanged();
      }
    }.schedule(0);
  }

  interface CustomPageViewImplUiBinder extends UiBinder<FlowPanel, CustomPageViewImpl> {}
}
