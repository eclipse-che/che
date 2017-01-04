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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.parts.base.BaseView;

import javax.validation.constraints.NotNull;

/**
 * @author Valeriy Svydenko
 */
@Singleton
public class RecipesPartViewImpl extends BaseView<RecipePartView.ActionDelegate> implements RecipePartView{
    interface RecipesPartViewImplUiBinder extends UiBinder<Widget, RecipesPartViewImpl> {
    }

    private static final RecipesPartViewImplUiBinder UI_BINDER = GWT.create(RecipesPartViewImplUiBinder.class);

    @UiField
    FlowPanel widgets;

    @Inject
    public RecipesPartViewImpl(org.eclipse.che.ide.Resources resources) {
        super(resources);

        setContentWidget(UI_BINDER.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Override
    public void addRecipe(@NotNull Widget recipe) {
        widgets.add(recipe);
    }

    /** {@inheritDoc} */
    @Override
    public void removeRecipe(@NotNull Widget recipe) {
        widgets.remove(recipe);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        widgets.clear();
    }
}