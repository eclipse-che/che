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
package org.eclipse.che.ide.command.editor.page.project;

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
 * Switcher widget which is associated with some project name.
 *
 * @author Artem Zatsarynnyi
 */
public class ProjectSwitcher extends Composite implements HasValue<Boolean> {

  private static final ProjectSwitcherUiBinder UI_BINDER =
      GWT.create(ProjectSwitcherUiBinder.class);

  @UiField Label label;

  @UiField Switcher switcher;

  ProjectSwitcher(String projectName) {
    initWidget(UI_BINDER.createAndBindUi(this));

    label.setText(projectName);
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

  interface ProjectSwitcherUiBinder extends UiBinder<Widget, ProjectSwitcher> {}
}
