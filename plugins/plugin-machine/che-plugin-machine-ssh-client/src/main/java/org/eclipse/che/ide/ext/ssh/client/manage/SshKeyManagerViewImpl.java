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
package org.eclipse.che.ide.ext.ssh.client.manage;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.ide.ext.ssh.client.SshLocalizationConstant;
import org.eclipse.che.ide.ext.ssh.client.SshResources;
import org.eclipse.che.ide.ui.cellview.CellTableResources;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The implementation of {@link SshKeyManagerView}.
 *
 * @author Andrey Plotnikov
 */
@Singleton
public class SshKeyManagerViewImpl extends Composite implements SshKeyManagerView {
    interface SshKeyManagerViewImplUiBinder extends UiBinder<Widget, SshKeyManagerViewImpl> {}

    private static SshKeyManagerViewImplUiBinder uiBinder = GWT.create(SshKeyManagerViewImplUiBinder.class);

    @UiField
    Button                btnGenerate;
    @UiField
    Button                btnUpload;
    @UiField(provided = true)
    CellTable<SshPairDto> keys;
    @UiField(provided = true)
    final   SshResources            res;
    @UiField(provided = true)
    final   SshLocalizationConstant locale;
    private ActionDelegate          delegate;

    @Inject
    protected SshKeyManagerViewImpl(SshResources resources, SshLocalizationConstant locale, CellTableResources res) {
        this.res = resources;
        this.locale = locale;

        initSshKeyTable(res);
        initWidget(uiBinder.createAndBindUi(this));
    }

    /** Creates table what contains list of available ssh keys. */
    private void initSshKeyTable(final CellTable.Resources res) {
        keys = new CellTable<>(20, res);
        Column<SshPairDto, String> hostColumn = new Column<SshPairDto, String>(new TextCell()) {
            @Override
            public String getValue(SshPairDto object) {
                return object.getName();
            }

            @Override
            public void render(Context context, SshPairDto object, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div id=\"" + UIObject.DEBUG_ID_PREFIX + "-sshKeys-cellTable-title-" + context.getIndex() + "\">");
                super.render(context, object, sb);
            }
        };
        hostColumn.setSortable(true);

        Column<SshPairDto, String> publicKeyColumn = new Column<SshPairDto, String>(new ButtonCell()) {
            @Override
            public String getValue(SshPairDto object) {
                return "View";
            }

            @Override
            public void render(Context context, SshPairDto object, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div id=\"" + UIObject.DEBUG_ID_PREFIX + "-sshKeys-cellTable-key-" + context.getIndex() + "\">");
                if (object != null && object.getPublicKey() != null) {
                    super.render(context, object, sb);
                }
            }
        };
        // Creates handler on button clicked
        publicKeyColumn.setFieldUpdater(new FieldUpdater<SshPairDto, String>() {
            @Override
            public void update(int index, SshPairDto object, String value) {
                delegate.onViewClicked(object);
            }
        });

        Column<SshPairDto, String> deleteKeyColumn = new Column<SshPairDto, String>(new ButtonCell()) {
            @Override
            public String getValue(SshPairDto object) {
                return "Delete";
            }

            @Override
            public void render(Context context, SshPairDto object, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div id=\"" + UIObject.DEBUG_ID_PREFIX + "-sshKeys-cellTable-delete-" + context.getIndex() + "\">");
                super.render(context, object, sb);
            }
        };
        // Creates handler on button clicked
        deleteKeyColumn.setFieldUpdater(new FieldUpdater<SshPairDto, String>() {
            @Override
            public void update(int index, SshPairDto object, String value) {
                delegate.onDeleteClicked(object);
            }
        });

        keys.addColumn(hostColumn, "Title");
        keys.addColumn(publicKeyColumn, "Public Key");
        keys.addColumn(deleteKeyColumn, "Delete");
        keys.setColumnWidth(hostColumn, 50, Style.Unit.PCT);
        keys.setColumnWidth(publicKeyColumn, 30, Style.Unit.PX);
        keys.setColumnWidth(deleteKeyColumn, 30, Style.Unit.PX);

        // don't show loading indicator
        keys.setLoadingIndicator(null);
    }

    /** {@inheritDoc} */
    @Override
    public void setPairs(@NotNull List<SshPairDto> pairs) {
        this.keys.setRowData(pairs);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @UiHandler("btnGenerate")
    public void onGenerateClicked(ClickEvent event) {
        delegate.onGenerateClicked();
    }

    @UiHandler("btnUpload")
    public void onUpdateClicked(ClickEvent event) {
        delegate.onUploadClicked();
    }
}
