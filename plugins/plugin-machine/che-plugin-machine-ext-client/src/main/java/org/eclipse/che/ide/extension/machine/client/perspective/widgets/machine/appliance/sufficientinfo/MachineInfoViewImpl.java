/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.sufficientinfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;

import javax.validation.constraints.NotNull;

/**
 * The class contains methods which allow change view representation of machine's information.
 *
 * @author Dmitry Shnurenko
 */
public class MachineInfoViewImpl extends Composite implements MachineInfoView {
    interface MachineInfoViewImplUiBinder extends UiBinder<Widget, MachineInfoViewImpl> {
    }

    private final static MachineInfoViewImplUiBinder UI_BINDER = GWT.create(MachineInfoViewImplUiBinder.class);

    @UiField(provided = true)
    final MachineLocalizationConstant locale;

    @UiField
    Label name;
    @UiField
    Label machineId;
    @UiField
    Label owner;
    @UiField
    Label status;
    @UiField
    Label type;
    @UiField
    Label dev;
    @UiField
    Label workspaceId;

    @Inject
    public MachineInfoViewImpl(MachineLocalizationConstant locale) {
        this.locale = locale;

        initWidget(UI_BINDER.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Override
    public void updateInfo(MachineEntity machine) {
        name.setText(machine.getDisplayName());
        machineId.setText(machine.getId());
        status.setText(String.valueOf(machine.getStatus()));
        type.setText(machine.getType());
        dev.setText(String.valueOf(machine.isDev()));
    }

    /** {@inheritDoc} */
    @Override
    public void setOwner(@NotNull String ownerName) {
        owner.setText(ownerName);
    }

    /** {@inheritDoc} */
    @Override
    public void setWorkspaceName(@NotNull String workspaceName) {
        workspaceId.setText(workspaceName);
    }
}
