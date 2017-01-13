/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.part.explorer.project.synchronize;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;

import org.eclipse.che.ide.CoreLocalizationConstant;

import javax.validation.constraints.NotNull;

/**
 * The class represent widget which contains label with description and text field to input correct location.
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
