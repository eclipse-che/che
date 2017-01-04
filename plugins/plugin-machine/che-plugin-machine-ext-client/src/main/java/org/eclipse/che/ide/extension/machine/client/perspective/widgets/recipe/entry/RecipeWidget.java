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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.vectomatic.dom.svg.ui.SVGImage;

import javax.validation.constraints.NotNull;

/**
 * The class describes special widget which is entry in list of recipes.
 *
 * @author Valeriy Svydenko
 */
public class RecipeWidget extends Composite implements RecipeEntry, ClickHandler {
    interface RecipeEntryWidgetUiBinder extends UiBinder<Widget, RecipeWidget> {
    }

    private final static RecipeEntryWidgetUiBinder UI_BINDER = GWT.create(RecipeEntryWidgetUiBinder.class);

    @UiField
    SimpleLayoutPanel image;
    @UiField
    Label             name;
    @UiField
    FlowPanel         main;

    private final MachineResources resources;
    private final RecipeDescriptor descriptor;

    private ActionDelegate delegate;

    public RecipeWidget(RecipeDescriptor recipe, MachineResources resources) {
        this.resources = resources;
        this.descriptor = recipe;

        initWidget(UI_BINDER.createAndBindUi(this));

        SVGImage icon = new SVGImage(resources.recipe());
        image.getElement().appendChild(icon.getSvgElement().getElement());

        name.setText(recipe.getName());

        addDomHandler(this, ClickEvent.getType());
    }

    /** Returns the descriptor of current recipe */
    @NotNull
    public RecipeDescriptor getDescriptor() {
        return descriptor;
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(@NotNull ClickEvent event) {
        delegate.onRecipeClicked(this);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(@NotNull ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** Changes style of widget as selected */
    public void select() {
        main.removeStyleName(resources.getCss().unSelectRecipe());
        main.addStyleName(resources.getCss().selectRecipe());
    }

    /** Changes style of widget as unselected */
    public void unSelect() {
        main.removeStyleName(resources.getCss().selectRecipe());
        main.addStyleName(resources.getCss().unSelectRecipe());
    }

    /** Sets name of the Recipe. */
    public void setName(@NotNull String name) {
        this.name.setText(name);
    }

}