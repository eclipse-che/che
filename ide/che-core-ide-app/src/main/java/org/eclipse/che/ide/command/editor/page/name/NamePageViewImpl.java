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
package org.eclipse.che.ide.command.editor.page.name;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Implementation of {@link NamePageView}.
 *
 * @author Artem Zatsarynnyi
 */
public class NamePageViewImpl extends Composite implements NamePageView {

  private static final NamePageViewImplUiBinder UI_BINDER =
      GWT.create(NamePageViewImplUiBinder.class);

  @UiField TextBox commandName;

  @UiField Button runButton;

  private ActionDelegate delegate;

  @Inject
  public NamePageViewImpl() {
    initWidget(UI_BINDER.createAndBindUi(this));
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void setCommandName(String name) {
    commandName.setValue(name);
  }

  @Override
  public void setFocusOnName() {
    commandName.setFocus(true);
  }

  @UiHandler({"commandName"})
  void onNameChanged(KeyUpEvent event) {
    delegate.onNameChanged(commandName.getValue());
  }

  @UiHandler("runButton")
  public void handleRunButton(ClickEvent clickEvent) {
    delegate.onCommandRun();
  }

  interface NamePageViewImplUiBinder extends UiBinder<Widget, NamePageViewImpl> {}
}
