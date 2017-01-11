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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;

import javax.validation.constraints.NotNull;

/**
 * Class provides view representation of property button on properties panel.
 *
 * @author Valeriy Svydenko
 */
public class EditorButtonWidgetImpl extends Composite implements EditorButtonWidget, ClickHandler {
    interface EditorButtonImplUiBinder extends UiBinder<Widget, EditorButtonWidgetImpl> {
    }

    private static final EditorButtonImplUiBinder UI_BINDER = GWT.create(EditorButtonImplUiBinder.class);

    @UiField
    Label button;

    @UiField(provided = true)
    final MachineLocalizationConstant locale;
    @UiField(provided = true)
    final MachineResources            resources;

    private ActionDelegate delegate;
    private boolean        isEnable;

    @Inject
    public EditorButtonWidgetImpl(MachineLocalizationConstant locale,
                                  MachineResources resources,
                                  @Assisted String title,
                                  @Assisted Background background) {
        this.locale = locale;
        this.resources = resources;

        initWidget(UI_BINDER.createAndBindUi(this));

        getElement().getStyle().setBackgroundColor(background.toString());

        button.setText(title);

        addDomHandler(this, ClickEvent.getType());

        setEnable(true);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnable(boolean isEnable) {
        this.isEnable = isEnable;

        if (isEnable) {
            button.removeStyleName(resources.getCss().opacityButton());
        } else {
            button.addStyleName(resources.getCss().opacityButton());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(@NotNull ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(ClickEvent event) {
        if (isEnable) {
            delegate.onButtonClicked();
        }
    }

    public enum Background {
        BLACK("#313335"), GREY("#474747"), BLUE("#256c9f");

        private final String color;

        Background(@NotNull String color) {
            this.color = color;
        }

        /** @return value of background color */
        @Override
        @NotNull
        public String toString() {
            return color;
        }
    }
}