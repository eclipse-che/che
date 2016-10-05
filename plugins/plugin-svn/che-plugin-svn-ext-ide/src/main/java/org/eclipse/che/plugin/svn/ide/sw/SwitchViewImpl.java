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
package org.eclipse.che.plugin.svn.ide.sw;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;

/**
 * The implementation of {@link SwitchView}.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class SwitchViewImpl extends Window implements SwitchView {

    interface SwitchViewImplUiBinder extends UiBinder<Widget, SwitchViewImpl> {}

    private static SwitchViewImplUiBinder ourUiBinder = GWT.create(SwitchViewImplUiBinder.class);

    private final Button btnCancel;
    private final Button btnSwitch;

    @UiField
    RadioButton switchToTrunk;
    @UiField
    RadioButton switchToBranch;
    @UiField
    RadioButton switchToTag;
    @UiField
    RadioButton switchToLocation;

    @UiField(provided = true)
    SubversionExtensionResources             resources;
    @UiField(provided = true)
    SubversionExtensionLocalizationConstants constants;

    private ActionDelegate delegate;

    @Inject
    public SwitchViewImpl(final SubversionExtensionResources resources,
                          final SubversionExtensionLocalizationConstants constants) {
        this.resources = resources;
        this.constants = constants;

        this.ensureDebugId("svn-switch-window");

        this.setTitle(constants.switchDescription());
        this.setWidget(ourUiBinder.createAndBindUi(this));

        btnCancel = createButton(constants.buttonCancel(), "svn-switch-cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        addButtonToFooter(btnCancel);

        btnSwitch = createButton(constants.buttonSwitch(), "svn-switch-switch", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onSwitchClicked();
            }
        });
        addButtonToFooter(btnSwitch);
    }

    @Override
    public void setDelegate(final ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isSwitchToTrunk() {
        return switchToTrunk.getValue();
    }

    @Override
    public boolean isSwitchToBranch() {
        return switchToBranch.getValue();
    }

    @Override
    public boolean isSwitchToTag() {
        return switchToTag.getValue();
    }

    @Override
    public boolean isSwitchToLocation() {
        return switchToLocation.getValue();
    }

    @Override
    public void close() {
        this.hide();
    }

    @Override
    public void showWindow() {
        this.show();
    }

    @Override
    protected void onClose() { }

    @UiHandler("switchToBranch")
    public void onSwitchToBranchClicked(final ClickEvent event) { delegate.onSwitchToBranchChanged(); }

    @UiHandler("switchToTrunk")
    public void onSwitchToTrunkClicked(final ClickEvent event) {
        delegate.onSwitchToTrunkChanged();
    }

    @UiHandler("switchToTag")
    public void onSwitchToTagClicked(final ClickEvent event) {
        delegate.onSwitchToTagChanged();
    }

    @UiHandler("switchToLocation")
    public void onSwitchToLocationClicked(final ClickEvent event) {
        delegate.onSwitchToLocationChanged();
    }
}
