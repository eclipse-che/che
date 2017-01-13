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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.container;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.validation.constraints.NotNull;

import static com.google.gwt.dom.client.Style.Unit.PCT;

/**
 * The container for recipes panels.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class RecipesContainerViewImpl extends Composite implements RecipesContainerView {
    interface PropertiesContainerViewImplUiBinder extends UiBinder<Widget, RecipesContainerViewImpl> {
    }

    private static final PropertiesContainerViewImplUiBinder UI_BINDER = GWT.create(PropertiesContainerViewImplUiBinder.class);

    @UiField
    SimplePanel mainPanel;

    @Inject
    public RecipesContainerViewImpl() {
        initWidget(UI_BINDER.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Override
    protected void onAttach() {
        super.onAttach();
        Style style = getElement().getParentElement().getParentElement().getStyle();
        style.setHeight(100, PCT);
        style.setWidth(100, PCT);
    }

    /** {@inheritDoc} */
    @Override
    public void showWidget(@NotNull IsWidget panel) {
        mainPanel.setWidget(panel);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        //do nothing
    }
}