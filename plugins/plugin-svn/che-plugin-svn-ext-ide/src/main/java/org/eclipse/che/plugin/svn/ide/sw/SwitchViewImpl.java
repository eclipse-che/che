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
package org.eclipse.che.plugin.svn.ide.sw;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;

import java.util.List;

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
    RadioButton switchToOtherLocation;
    @UiField
    ListBox     switchToLocation;
    @UiField
    TextBox     location;
    @UiField
    Button      selectOtherLocation;

    @UiField
    CheckBox ignoreAncestry;
    @UiField
    CheckBox force;
    @UiField
    CheckBox ignoreExternals;

    @UiField
    RadioButton switchToHeadRevision;
    @UiField
    RadioButton switchToRevision;
    @UiField
    TextBox     revision;

    @UiField
    ListBox depth;
    @UiField
    ListBox workingCopyDepth;
    @UiField
    ListBox accept;

    @UiField(provided = true)
    SubversionExtensionResources             resources;
    @UiField(provided = true)
    SubversionExtensionLocalizationConstants constants;

    private ActionDelegate delegate;

    @Inject
    public SwitchViewImpl(SubversionExtensionResources resources, SubversionExtensionLocalizationConstants constants) {
        this.resources = resources;
        this.constants = constants;

        this.ensureDebugId("svn-switch-window");

        this.setTitle(constants.switchDescription());
        this.setWidget(ourUiBinder.createAndBindUi(this));

        this.depth.addItem("", "");
        this.depth.addItem(this.constants.subversionDepthInfinityLabel(), "infinity");
        this.depth.addItem(this.constants.subversionDepthImmediatesLabel(), "immediates");
        this.depth.addItem(this.constants.subversionDepthFilesLabel(), "files");
        this.depth.addItem(this.constants.subversionDepthEmptyLabel(), "empty");
        this.depth.setSelectedIndex(1);

        this.workingCopyDepth.addItem("", "");
        this.workingCopyDepth.addItem(this.constants.subversionWorkingCopyDepthInfinityLabel(), "infinity");
        this.workingCopyDepth.addItem(this.constants.subversionWorkingCopyDepthImmediatesLabel(), "immediates");
        this.workingCopyDepth.addItem(this.constants.subversionWorkingCopyDepthFilesLabel(), "files");
        this.workingCopyDepth.addItem(this.constants.subversionWorkingCopyDepthEmptyLabel(), "empty");
        this.workingCopyDepth.setSelectedIndex(0);

        this.accept.addItem(this.constants.subversionAcceptPostponeLabel(), "postpone");
        this.accept.addItem(this.constants.subversionAcceptMineFullLabel(), "mine-full");
        this.accept.addItem(this.constants.subversionAcceptTheirsFullLabel(), "theirs-full");

        btnCancel = createButton(constants.buttonCancel(), "svn-switch-cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        addButtonToFooter(btnCancel);

        btnSwitch = createPrimaryButton(constants.buttonSwitch(), "svn-switch-switch", new ClickHandler() {
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
    public boolean isSwitchToOtherLocation() {
        return switchToOtherLocation.getValue();
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
    public void setPredefinedLocations(List<String> locations) {
        switchToLocation.clear();
        for (String l : locations) {
            switchToLocation.addItem(l);
        }
    }

    @Override
    public String getSwitchToLocation() {
        return switchToLocation.getSelectedValue();
    }

    @Override
    public void setLocation(String location) {
        this.location.setValue(location);
    }

    @Override
    public void setLocationEnabled(boolean enabled) {
        location.setEnabled(enabled);
    }

    @Override
    public String getLocation() {
        return location.getValue();
    }

    @Override
    public void setSwitchToLocationEnabled(boolean enabled) {
        switchToLocation.setEnabled(enabled);
    }

    @Override
    public boolean isIgnoreAncestry() {
        return ignoreAncestry.getValue();
    }

    @Override
    public boolean isForce() {
        return force.getValue();
    }

    @Override
    public boolean isIgnoreExternals() {
        return ignoreExternals.getValue();
    }

    @Override
    public void setSwitchRevisionEnabled(boolean enabled) {
        revision.setEnabled(enabled);
    }

    @Override
    public String getRevision() {
        return revision.getText();
    }

    @Override
    public boolean isSwitchToRevision() {
        return switchToRevision.getValue();
    }

    @Override
    public boolean isSwitchToHeadRevision() {
        return switchToHeadRevision.getValue();
    }

    @Override
    public void setSwitchButtonEnabled(boolean enabled) {
        btnSwitch.setEnabled(enabled);
    }

    @Override
    public void setSelectOtherLocationButtonEnabled(boolean enabled) {
        selectOtherLocation.setEnabled(enabled);
    }

    @Override
    public String getDepth() {
        return depth.getSelectedValue();
    }

    @Override
    public String getWorkingCopyDepth() {
        return workingCopyDepth.getSelectedValue();
    }

    @Override
    public String getAccept() {
        return accept.getSelectedValue();
    }

    @Override
    public void setDepthEnabled(boolean enabled) {
        depth.setEnabled(enabled);
    }

    @Override
    public void setWorkingCopyDepthEnabled(boolean enabled) {
        workingCopyDepth.setEnabled(enabled);
    }

    @UiHandler("switchToBranch")
    public void onSwitchToBranchClicked(final ClickEvent event) {
        delegate.onSwitchToBranchChanged();
    }

    @UiHandler("switchToTrunk")
    public void onSwitchToTrunkClicked(final ClickEvent event) {
        delegate.onSwitchToTrunkChanged();
    }

    @UiHandler("switchToTag")
    public void onSwitchToTagClicked(final ClickEvent event) {
        delegate.onSwitchToTagChanged();
    }

    @UiHandler("switchToOtherLocation")
    public void onSwitchToLocationClicked(final ClickEvent event) {
        delegate.onSwitchToOtherLocationChanged();
    }

    @UiHandler("switchToLocation")
    public void onSwitchLocationChanged(final ChangeEvent event) {
        delegate.onSwitchLocationChanged();
    }

    @UiHandler("switchToHeadRevision")
    public void onSwitchToHeadRevisionClicked(final ClickEvent event) {
        delegate.onSwitchToHeadRevisionChanged();
    }

    @UiHandler("switchToRevision")
    public void onSwitchToRevisionClicked(final ClickEvent event) {
        delegate.onSwitchToRevisionChanged();
    }

    @UiHandler("revision")
    public void onSwitchRevisionChanged(final KeyUpEvent event) {
        delegate.onRevisionUpdated();
    }

    @UiHandler("selectOtherLocation")
    public void onSelectOtherLocationClicked(final ClickEvent event) {
        delegate.onSelectOtherLocationClicked();
    }

    @UiHandler("depth")
    public void onDepthChanged(final ChangeEvent event) {
        delegate.onDepthChanged();
    }

    @UiHandler("workingCopyDepth")
    public void onWorkingCopyDepthChanged(final ChangeEvent event) {
        delegate.onWorkingCopyDepthChanged();
    }

}
