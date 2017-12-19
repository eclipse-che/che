/*
 * Copyright (c) 2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.editor.page.renderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.eclipse.che.ide.ui.switcher.Switcher;

/**
 * Switcher widget which is associated with some renderer name.
 *
 * @author Victor Rubezhny
 */
public class RendererSwitcher extends Composite implements HasValue<Boolean> {

  private static final ProjectSwitcherUiBinder UI_BINDER =
      GWT.create(ProjectSwitcherUiBinder.class);

  @UiField Label label;

  @UiField Switcher switcher;

  RendererSwitcher(String rendererName) {
    initWidget(UI_BINDER.createAndBindUi(this));

    label.setText(rendererName);
  }

  @Override
  public Boolean getValue() {
    return switcher.getValue();
  }

  @Override
  public void setValue(Boolean value) {
    switcher.setValue(value);
  }

  @Override
  public void setValue(Boolean value, boolean fireEvents) {
    switcher.setValue(value, fireEvents);
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
    return switcher.addValueChangeHandler(handler);
  }

  interface ProjectSwitcherUiBinder extends UiBinder<Widget, RendererSwitcher> {}
}
