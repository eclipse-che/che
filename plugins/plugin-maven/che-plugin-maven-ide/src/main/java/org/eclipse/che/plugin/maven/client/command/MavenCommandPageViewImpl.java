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
package org.eclipse.che.plugin.maven.client.command;

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
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;

/**
 * The implementation of {@link MavenCommandPageView}.
 *
 * @author Artem Zatsarynnyi
 */
public class MavenCommandPageViewImpl implements MavenCommandPageView {

  private static final MavenPageViewImplUiBinder UI_BINDER =
      GWT.create(MavenPageViewImplUiBinder.class);

  private final FlowPanel rootElement;

  @UiField TextBox workingDirectory;
  @UiField TextBox arguments;

  @UiField(provided = true)
  MavenLocalizationConstant locale;

  private ActionDelegate delegate;

  @Inject
  public MavenCommandPageViewImpl(MavenLocalizationConstant locale) {
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
  public String getArguments() {
    return arguments.getValue();
  }

  // Note that Closure Compiler doesn't allow to use 'arguments' as a name of a method argument.
  @Override
  public void setArguments(String args) {
    this.arguments.setValue(args);
  }

  @UiHandler({"workingDirectory"})
  void onWorkingDirectoryChanged(KeyUpEvent event) {
    // workingDirectory value may not be updated immediately after keyUp
    // therefore use the timer with zero delay
    new Timer() {
      @Override
      public void run() {
        delegate.onWorkingDirectoryChanged();
      }
    }.schedule(0);
  }

  @UiHandler({"arguments"})
  void onArgumentsChanged(KeyUpEvent event) {
    // arguments value may not be updated immediately after keyUp
    // therefore use the timer with zero delay
    new Timer() {
      @Override
      public void run() {
        delegate.onArgumentsChanged();
      }
    }.schedule(0);
  }

  interface MavenPageViewImplUiBinder extends UiBinder<FlowPanel, MavenCommandPageViewImpl> {}
}
