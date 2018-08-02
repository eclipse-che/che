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
package org.eclipse.che.ide.ext.java.client.command;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;

/**
 * The implementation of {@link JavaCommandPageView}.
 *
 * @author Valeriy Svydenko
 */
public class JavaCommandPageViewImpl implements JavaCommandPageView {

  private static final JavaCommandPageViewImplUiBinder UI_BINDER =
      GWT.create(JavaCommandPageViewImplUiBinder.class);

  private final FlowPanel rootElement;

  @UiField TextBox mainClass;
  @UiField TextArea commandLine;

  @UiField(provided = true)
  JavaLocalizationConstant locale;

  @UiField Button browseMainClass;

  private ActionDelegate delegate;

  @Inject
  public JavaCommandPageViewImpl(JavaLocalizationConstant locale) {
    this.locale = locale;

    rootElement = UI_BINDER.createAndBindUi(this);

    browseMainClass.addClickHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            delegate.onAddMainClassBtnClicked();
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
  public String getProject() {
    return null;
  }

  @Override
  public void setProject(String workingDirectory) {}

  @Override
  public String getMainClass() {
    return this.mainClass.getValue();
  }

  @Override
  public void setMainClass(String mainClass) {
    this.mainClass.setValue(mainClass);
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
  void onCommandLineChanged(KeyUpEvent event) {
    // commandLine value may not be updated immediately after keyUp
    // therefore use the timer with delay=0
    new Timer() {
      @Override
      public void run() {
        delegate.onCommandLineChanged();
      }
    }.schedule(0);
  }

  interface JavaCommandPageViewImplUiBinder extends UiBinder<FlowPanel, JavaCommandPageViewImpl> {}
}
