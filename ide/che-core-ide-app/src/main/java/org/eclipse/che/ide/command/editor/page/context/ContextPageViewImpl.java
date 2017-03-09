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
package org.eclipse.che.ide.command.editor.page.context;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Implementation of {@link ContextPageView}.
 *
 * @author Artem Zatsarynnyi
 */
public class ContextPageViewImpl extends Composite implements ContextPageView {

    private static final ContextPageViewImplUiBinder UI_BINDER = GWT.create(ContextPageViewImplUiBinder.class);

    @UiField
    SimpleLayoutPanel mainPanel;

    @UiField
    CheckBox workspaceCheckBox;

    private ActionDelegate delegate;

    @Inject
    public ContextPageViewImpl() {
        initWidget(UI_BINDER.createAndBindUi(this));
    }

    @Override
    public void setWorkspace(boolean value) {
        workspaceCheckBox.setValue(value);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @UiHandler({"workspaceCheckBox"})
    void onWorkspaceChanged(ValueChangeEvent<Boolean> event) {
        delegate.onWorkspaceChanged(event.getValue());
    }

    interface ContextPageViewImplUiBinder extends UiBinder<Widget, ContextPageViewImpl> {
    }
}
