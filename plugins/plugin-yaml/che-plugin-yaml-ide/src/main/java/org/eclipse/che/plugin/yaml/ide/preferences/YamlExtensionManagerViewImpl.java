/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.yaml.ide.preferences;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ui.cellview.CellTableResources;
import org.eclipse.che.plugin.yaml.ide.YamlLocalizationConstant;
import org.eclipse.che.plugin.yaml.shared.YamlPreference;

/**
 * The implementation of {@link YamlExtensionManagerView}.
 *
 * @author Joshua Pinkney
 */
@Singleton
public class YamlExtensionManagerViewImpl extends Composite implements YamlExtensionManagerView {
  interface YamlExtensionManagerViewImplUiBinder
      extends UiBinder<Widget, YamlExtensionManagerViewImpl> {}

  private static YamlExtensionManagerViewImplUiBinder uiBinder =
      GWT.create(YamlExtensionManagerViewImplUiBinder.class);

  @UiField Button addUrl;

  @UiField(provided = true)
  CellTable<YamlPreference> yamlPreferenceCellTable;

  @UiField Label headerUiMsg;

  private ActionDelegate delegate;

  private YamlLocalizationConstant local;

  @Inject
  protected YamlExtensionManagerViewImpl(CellTableResources res, YamlLocalizationConstant local) {
    this.local = local;
    initYamlExtensionTable(res);
    initWidget(uiBinder.createAndBindUi(this));
  }

  /**
   * Creates table which contains list of available preferences
   *
   * @param res Celltable resources
   */
  private void initYamlExtensionTable(final CellTable.Resources res) {

    yamlPreferenceCellTable = new CellTable<YamlPreference>(20, res);
    Column<YamlPreference, String> urlColumn =
        new Column<YamlPreference, String>(new EditTextCell()) {
          @Override
          public String getValue(YamlPreference object) {
            return object.getUrl();
          }

          @Override
          public void render(Context context, YamlPreference object, SafeHtmlBuilder sb) {
            sb.appendHtmlConstant(
                "<div id=\""
                    + UIObject.DEBUG_ID_PREFIX
                    + "-preferences-cellTable-url-"
                    + context.getIndex()
                    + "\">");
            super.render(context, object, sb);
          }
        };

    urlColumn.setFieldUpdater(
        new FieldUpdater<YamlPreference, String>() {
          @Override
          public void update(int index, YamlPreference object, String value) {
            object.setUrl(value);
            delegate.nowDirty();
          }
        });

    Column<YamlPreference, String> globColumn =
        new Column<YamlPreference, String>(new EditTextCell()) {
          @Override
          public String getValue(YamlPreference object) {
            return object.getGlob();
          }

          @Override
          public void render(Context context, YamlPreference object, SafeHtmlBuilder sb) {
            sb.appendHtmlConstant(
                "<div id=\""
                    + UIObject.DEBUG_ID_PREFIX
                    + "-preferences-cellTable-glob-"
                    + context.getIndex()
                    + "\">");
            if (object != null) {
              super.render(context, object, sb);
            }
          }
        };

    globColumn.setFieldUpdater(
        new FieldUpdater<YamlPreference, String>() {
          @Override
          public void update(int index, YamlPreference object, String value) {
            object.setGlob(value);
            delegate.nowDirty();
          }
        });

    Column<YamlPreference, String> deletePreferenceColumn =
        new Column<YamlPreference, String>(new ButtonCell()) {
          @Override
          public String getValue(YamlPreference object) {
            return "Delete";
          }

          @Override
          public void render(Context context, YamlPreference object, SafeHtmlBuilder sb) {
            sb.appendHtmlConstant(
                "<div id=\""
                    + UIObject.DEBUG_ID_PREFIX
                    + "-preferences-cellTable-delete-"
                    + context.getIndex()
                    + "\">");
            super.render(context, object, sb);
          }
        };

    // Creates handler on button clicked
    deletePreferenceColumn.setFieldUpdater(
        new FieldUpdater<YamlPreference, String>() {
          @Override
          public void update(int index, YamlPreference object, String value) {
            delegate.onDeleteClicked(object);
          }
        });

    yamlPreferenceCellTable.addColumn(urlColumn, local.urlColumnHeader());
    yamlPreferenceCellTable.addColumn(globColumn, local.globColumnHeader());
    yamlPreferenceCellTable.addColumn(deletePreferenceColumn, local.deleteColumnHeader());
    yamlPreferenceCellTable.setWidth("100%", true);
    yamlPreferenceCellTable.setColumnWidth(urlColumn, 45, Style.Unit.PCT);
    yamlPreferenceCellTable.setColumnWidth(globColumn, 30, Style.Unit.PCT);
    yamlPreferenceCellTable.setColumnWidth(deletePreferenceColumn, 25, Style.Unit.PCT);

    // don't show loading indicator
    yamlPreferenceCellTable.setLoadingIndicator(null);
  }

  /** {@inheritDoc} */
  @Override
  public void setPairs(@NotNull List<YamlPreference> pairs) {
    this.yamlPreferenceCellTable.setRowData(pairs);
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @UiHandler("addUrl")
  public void onAddUrlClicked(ClickEvent event) {
    delegate.onAddUrlClicked();
  }
}
