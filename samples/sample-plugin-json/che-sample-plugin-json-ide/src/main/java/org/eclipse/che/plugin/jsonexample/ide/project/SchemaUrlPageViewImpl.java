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
package org.eclipse.che.plugin.jsonexample.ide.project;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;

/**
 * Implementation of the {@link SchemaUrlPageView}.
 */
class SchemaUrlPageViewImpl extends Composite implements SchemaUrlPageView {

    @UiField
    TextBox schemaUrl;
    private SchemaUrlChangedDelegate delegate;

    /**
     * Constructor.
     *
     * @param uiBinder
     *         the UI binder that initializes the page
     */
    @Inject
    public SchemaUrlPageViewImpl(JsonExamplePageViewUiBinder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(SchemaUrlChangedDelegate delegate) {
        this.delegate = delegate;
    }

    /**
     * Update handler for the schema URL field.
     *
     * @param event
     *         the event that caused the schemaUrl field to update
     */
    @UiHandler("schemaUrl")
    void onSchemaUrlChanged(KeyUpEvent event) {
        delegate.schemaUrlChanged(schemaUrl.getValue());
    }

    /**
     * UI binder for our page.
     */
    interface JsonExamplePageViewUiBinder extends UiBinder<DockLayoutPanel, SchemaUrlPageViewImpl> {
    }
}
