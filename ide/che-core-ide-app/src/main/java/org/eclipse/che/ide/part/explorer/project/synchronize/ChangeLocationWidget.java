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
package org.eclipse.che.ide.part.explorer.project.synchronize;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;

/**
 * The class represent widget which contains label with description and text field to input correct
 * location.
 *
 * @author Dmitry Shnurenko
 */
class ChangeLocationWidget extends FlowPanel {

  private final TextBox textBox;

  @Inject
  public ChangeLocationWidget(TextBox textBox, Label label, CoreLocalizationConstant locale) {
    this.textBox = textBox;

    label.setText(locale.locationIncorrect());
    textBox.setWidth("420px");

    add(label);
    add(textBox);
  }

  @NotNull
  public String getText() {
    return textBox.getText();
  }
}
