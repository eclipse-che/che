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
package org.eclipse.che.plugin.docker.ext.client.manage;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.ui.cellview.CellTableResources;
import org.eclipse.che.plugin.docker.client.dto.AuthConfig;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of {@link CredentialsPreferencesView}
 *
 * @author Sergii Leschenko
 */
public class CredentialsPreferencesViewImpl implements CredentialsPreferencesView {
    interface CredentialsPreferencesViewImplUiBinder extends UiBinder<DockLayoutPanel, CredentialsPreferencesViewImpl> {
    }

    private final DockLayoutPanel rootElement;

    private ActionDelegate delegate;

    @UiField
    Button addRegistryButton;

    @UiField
    Button addAccountButton;

    @UiField(provided = true)
    CellTable<AuthConfig> keys;

    @Inject
    public CredentialsPreferencesViewImpl(CredentialsPreferencesViewImplUiBinder uiBinder, CellTableResources res) {
        initCredentialsTable(res);
        rootElement = uiBinder.createAndBindUi(this);
    }

    private void initCredentialsTable(CellTable.Resources res) {
        keys = new CellTable<>(15, res);
        Column<AuthConfig, String> serverAddressColumn = new Column<AuthConfig, String>(new TextCell()) {
            @Override
            public String getValue(AuthConfig object) {
                return object.getServeraddress();
            }

            @Override
            public void render(Cell.Context context, AuthConfig object, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div id=\"" + UIObject.DEBUG_ID_PREFIX + "preferences-docker-credentials-cellTable-serveraddress-"
                                      + context.getIndex() + "\">");
                super.render(context, object, sb);
            }
        };
        serverAddressColumn.setSortable(true);

        Column<AuthConfig, String> editColumn = new Column<AuthConfig, String>(new ButtonCell()) {
            @Override
            public String getValue(AuthConfig object) {
                return "Edit";
            }

            @Override
            public void render(Cell.Context context, AuthConfig object, SafeHtmlBuilder sb) {
                if (object != null) {
                    sb.appendHtmlConstant("<div id=\"" + UIObject.DEBUG_ID_PREFIX + "preferences-docker-credentials-cellTable-edit-"
                                          + context.getIndex() + "\">");
                    super.render(context, object, sb);
                }
            }
        };
        // Creates handler on button clicked
        editColumn.setFieldUpdater(new FieldUpdater<AuthConfig, String>() {
            @Override
            public void update(int index, AuthConfig object, String value) {
                delegate.onEditClicked(object);
            }
        });

        Column<AuthConfig, String> deleteColumn = new Column<AuthConfig, String>(new ButtonCell()) {
            @Override
            public String getValue(AuthConfig object) {
                return "Delete";
            }

            @Override
            public void render(Cell.Context context, AuthConfig object, SafeHtmlBuilder sb) {
                if (object != null) {
                    sb.appendHtmlConstant(
                            "<div id=\"" + UIObject.DEBUG_ID_PREFIX + "preferences-docker-credentials-cellTable-delete-"
                            + context.getIndex() + "\">");
                    super.render(context, object, sb);
                }
            }
        };
        // Creates handler on button clicked
        deleteColumn.setFieldUpdater(new FieldUpdater<AuthConfig, String>() {
            @Override
            public void update(int index, AuthConfig object, String value) {
                delegate.onDeleteClicked(object);
            }
        });

        keys.addColumn(serverAddressColumn, "Server Address");
        keys.addColumn(editColumn, "");//Do not show label for edit column
        keys.addColumn(deleteColumn, "");//Do not show label for delete column
        keys.setColumnWidth(serverAddressColumn, 70, Style.Unit.PCT);
        keys.setColumnWidth(editColumn, 20, Style.Unit.PX);
        keys.setColumnWidth(deleteColumn, 20, Style.Unit.PX);

        // don't show loading indicator
        keys.setLoadingIndicator(null);
    }

    @Override
    public void setKeys(@NotNull Collection<AuthConfig> keys) {
        List<AuthConfig> appList = new ArrayList<>();
        for (AuthConfig key : keys) {
            appList.add(key);
        }

        this.keys.setRowData(appList);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return rootElement;
    }

    @UiHandler("addRegistryButton")
    void onSaveRegistryClicked(ClickEvent event) {
        delegate.onAddClicked();
    }

    @UiHandler("addAccountButton")
    void onSaveAccountClicked(ClickEvent event) {
        delegate.onAddAccountClicked();
    }
}
