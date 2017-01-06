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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.recipe;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import javax.validation.constraints.NotNull;

/**
 * The class displays current machine recipe.
 *
 * @author Valeriy Svydenko
 */
public class RecipeViewImpl extends Composite implements RecipeView {
    interface RecipeWidgetImplUiBinder extends UiBinder<Widget, RecipeViewImpl> {
    }

    private final static RecipeWidgetImplUiBinder UI_BINDER = GWT.create(RecipeWidgetImplUiBinder.class);
    @UiField
    TextArea script;

    @Inject
    public RecipeViewImpl() {
        initWidget(UI_BINDER.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Override
    public void setScript(@NotNull String script) {
        this.script.setText(script);
    }
}