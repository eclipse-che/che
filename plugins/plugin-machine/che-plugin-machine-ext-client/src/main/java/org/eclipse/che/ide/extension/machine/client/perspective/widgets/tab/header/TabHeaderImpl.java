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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.extension.machine.client.MachineResources;

import javax.validation.constraints.NotNull;

/**
 * The class provides methods to control view representation of tab's header.
 *
 * @author Dmitry Shnurenko
 */

public class TabHeaderImpl extends Composite implements TabHeader, ClickHandler {
    interface TabHeaderImplUiBinder extends UiBinder<Widget, TabHeaderImpl> {
    }

    private final static TabHeaderImplUiBinder UI_BINDER = GWT.create(TabHeaderImplUiBinder.class);

    private final MachineResources resources;
    private final String           name;

    @UiField
    Label tabName;

    private ActionDelegate delegate;

    @Inject
    public TabHeaderImpl(MachineResources resources, @Assisted String tabName) {
        this.resources = resources;

        initWidget(UI_BINDER.createAndBindUi(this));

        this.name = tabName;
        this.tabName.setText(tabName);

        addDomHandler(this, ClickEvent.getType());
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(ClickEvent event) {
        delegate.onTabClicked(tabName.getText());
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(@NotNull ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void setEnable() {
        removeStyleName(resources.getCss().disableTab());
        addStyleName(resources.getCss().activeTab());
        addStyleName(resources.getCss().activeTabText());
    }

    /** {@inheritDoc} */
    @Override
    public void setDisable() {
        removeStyleName(resources.getCss().activeTab());
        removeStyleName(resources.getCss().activeTabText());
        addStyleName(resources.getCss().disableTab());
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public String getName() {
        return name;
    }
}