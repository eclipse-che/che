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
package org.eclipse.che.ide.workspace.start.workspacewidget;

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

import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;

/**
 * The class represents workspace widget and contains methods to control
 * @author Dmitry Shnurenko
 */
public class WorkspaceWidgetImpl extends Composite implements WorkspaceWidget, ClickHandler {
    interface WorkspaceWidgetImplUiBinder extends UiBinder<Widget, WorkspaceWidgetImpl> {
    }

    private static final WorkspaceWidgetImplUiBinder UI_BINDER = GWT.create(WorkspaceWidgetImplUiBinder.class);

    private final WorkspaceDto workspace;

    private ActionDelegate delegate;

    @UiField
    Label name;
    @UiField
    Label status;

    @Inject
    public WorkspaceWidgetImpl(@Assisted WorkspaceDto workspace) {
        this.workspace = workspace;

        initWidget(UI_BINDER.createAndBindUi(this));

        name.setText(workspace.getConfig().getDefaultEnv());
        status.setText(workspace.getStatus().toString());

        addDomHandler(this, ClickEvent.getType());
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(ClickEvent event) {
        delegate.onWorkspaceSelected(workspace);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }
}