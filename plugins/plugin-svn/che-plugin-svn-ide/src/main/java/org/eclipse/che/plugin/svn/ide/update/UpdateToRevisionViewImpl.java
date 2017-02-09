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
package org.eclipse.che.plugin.svn.ide.update;

import org.eclipse.che.ide.ui.window.Window;
import com.google.gwt.core.client.GWT;
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

import javax.validation.constraints.NotNull;

import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;

/**
 * The implementation of {@link UpdateToRevisionView}.
 */
@Singleton
public class UpdateToRevisionViewImpl extends Window implements UpdateToRevisionView {

    interface UpdateToRevisionViewImplUiBinder extends UiBinder<Widget, UpdateToRevisionViewImpl> { }

    private static UpdateToRevisionViewImplUiBinder ourUiBinder = GWT.create(UpdateToRevisionViewImplUiBinder.class);

    Button btnCancel;
    Button btnUpdate;
    @UiField
    ListBox     depth;
    @UiField
    CheckBox    ignoreExternals;
    @UiField
    RadioButton headRevision;
    @UiField
    RadioButton customRevision;
    @UiField
    TextBox     revision;

    @UiField(provided = true)
    SubversionExtensionResources             resources;
    @UiField(provided = true)
    SubversionExtensionLocalizationConstants constants;

    private ActionDelegate delegate;

    @Inject
    public UpdateToRevisionViewImpl(final SubversionExtensionResources resources,
                                    final SubversionExtensionLocalizationConstants constants) {
        this.resources = resources;
        this.constants = constants;

        this.ensureDebugId("svn-update-to-revision-window");

        this.setTitle(constants.updateToRevisionTitle());
        this.setWidget(ourUiBinder.createAndBindUi(this));

        // Populate the 'Checkout Depth' list
        this.depth.addItem(this.constants.subversionDepthInfinityLabel(), "infinity");
        this.depth.addItem(this.constants.subversionDepthImmediatesLabel(), "immediates");
        this.depth.addItem(this.constants.subversionDepthFilesLabel(), "files");
        this.depth.addItem(this.constants.subversionDepthEmptyLabel(), "empty");

        btnCancel = createButton(constants.buttonCancel(), "svn-update-cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        addButtonToFooter(btnCancel);

        btnUpdate = createButton(constants.buttonUpdate(), "svn-update-update", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onUpdateClicked();
            }
        });
        addButtonToFooter(btnUpdate);
    }

    @Override
    public void setDelegate(final ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getDepth() {
        final int sIndex = this.depth.getSelectedIndex();

        return sIndex > -1 ? this.depth.getValue(sIndex) : "";
    }

    @Override
    public void setDepth(@NotNull String depth) {
        for (int i = 0; i < this.depth.getItemCount(); i++) {
            if (this.depth.getValue(i).equals(depth)) {
                this.depth.setSelectedIndex(i);
                break;
            }
        }
    }

    @Override
    public boolean ignoreExternals() {
        return this.ignoreExternals.getValue();
    }

    @Override
    public void setIgnoreExternals(boolean ignoreExternals) {
        this.ignoreExternals.setValue(ignoreExternals);
    }

    @Override
    public boolean isHeadRevision() {
        return this.headRevision.getValue();
    }

    @Override
    public void setIsHeadRevision(boolean headRevision) {
        this.headRevision.setValue(headRevision);
    }

    @Override
    public boolean isCustomRevision() {
        return this.customRevision.getValue();
    }

    @Override
    public void setIsCustomRevision(boolean customRevision) {
        this.customRevision.setValue(customRevision);
    }

    @Override
    public String getRevision() {
        return this.revision.getText();
    }

    @Override
    public void setRevision(String revision) {
        this.revision.setText(revision);
    }

    @Override
    public void setEnableUpdateButton(boolean enable) {
        this.btnUpdate.setEnabled(enable);
    }

    @Override
    public void setEnableCustomRevision(boolean enable) {
        this.revision.setEnabled(enable);
    }

    @Override
    public void close() {
        this.hide();
    }

    @Override
    public void showWindow() {
        this.show();
    }

    /**
     * @see ActionDelegate#onRevisionTypeChanged()
     *
     * @param event the click event
     */
    @UiHandler("headRevision")
    public void onHeadRevisionClicked(final ClickEvent event) {
        delegate.onRevisionTypeChanged();
    }

    /**
     * @see ActionDelegate#onRevisionTypeChanged()
     *
     * @param event the click event
     */
    @UiHandler("customRevision")
    public void onCustomRevisionClicked(final ClickEvent event) {
        delegate.onRevisionTypeChanged();
    }

    /**
     * @see ActionDelegate#onRevisionChanged() ()
     *
     * @param event the change event
     */
    @UiHandler("revision")
    public void onRevisionChanged(final KeyUpEvent event) {
        delegate.onRevisionChanged();
    }

}
