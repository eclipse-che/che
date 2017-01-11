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
package org.eclipse.che.ide.workspace.create.recipewidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * The class stores information about mainPanel and provides methods to get or change it.
 *
 * @author Dmitry Shnurenko
 */
public class RecipeWidgetImpl extends Composite implements RecipeWidget, ClickHandler {
    interface TagEntryUiBinder extends UiBinder<Widget, RecipeWidgetImpl> {
    }

    private static final TagEntryUiBinder UI_BINDER = GWT.create(TagEntryUiBinder.class);

    private final RecipeDescriptor descriptor;

    private ActionDelegate delegate;

    @UiField
    Label       tagName;
    @UiField
    SimplePanel icon;
    @UiField
    Label       type;
    @UiField
    FlowPanel   main;

    @Inject
    public RecipeWidgetImpl(org.eclipse.che.ide.Resources resources, @Assisted RecipeDescriptor descriptor) {
        this.descriptor = descriptor;

        initWidget(UI_BINDER.createAndBindUi(this));

        tagName.setText(descriptor.getName());
        type.setText(descriptor.getType());

        SVGImage image = new SVGImage(resources.recipe());
        icon.getElement().setInnerHTML(image.toString());

        addDomHandler(this, ClickEvent.getType());
    }

    /** {@inheritDoc} */
    @Override
    public String getRecipeUrl() {
        return descriptor.getLink("get recipe script").getHref();
    }

    /** {@inheritDoc} */
    @Override
    public String getTagName() {
        return tagName.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(ClickEvent event) {
        delegate.onTagClicked(this);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }
}