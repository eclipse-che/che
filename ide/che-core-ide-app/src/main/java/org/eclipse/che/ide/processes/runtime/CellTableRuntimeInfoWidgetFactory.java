/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.processes.runtime;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Style;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

/**
 * Creates a {@link CellTable} based widget to display information about runtime servers.
 *
 * @author Vlad Zhukovskyi
 * @since 5.18.0
 */
@Singleton
public class CellTableRuntimeInfoWidgetFactory implements RuntimeInfoWidgetFactory {

  private static final String DASH = "-";

  private final TableResources resources;
  private final RuntimeInfoLocalization locale;

  @Inject
  public CellTableRuntimeInfoWidgetFactory(
      TableResources resources, RuntimeInfoLocalization locale) {
    this.resources = resources;
    this.locale = locale;
  }

  private static String valueOrDefault(String value) {
    return isNullOrEmpty(value) ? DASH : value;
  }

  @Override
  public Widget create(String machineName, List<RuntimeInfo> runtimeList) {
    VerticalPanel panel = new VerticalPanel();

    panel.setWidth("100%");

    Label caption = new Label(locale.cellTableCaption(machineName));
    caption.addStyleName(resources.cellTableStyle().cellTableCaption());

    panel.add(caption);
    panel.add(createCellTable(runtimeList));

    return new ScrollPanel(panel);
  }

  private Widget createCellTable(List<RuntimeInfo> runtimeList) {
    CellTable<RuntimeInfo> table = new CellTable<>(100, resources);

    TextColumn<RuntimeInfo> referenceColumn =
        new TextColumn<RuntimeInfo>() {
          @Override
          public String getValue(RuntimeInfo record) {
            return valueOrDefault(record.getReference());
          }
        };

    TextColumn<RuntimeInfo> portColumn =
        new TextColumn<RuntimeInfo>() {
          @Override
          public String getValue(RuntimeInfo record) {
            return valueOrDefault(record.getPort());
          }
        };

    TextColumn<RuntimeInfo> protocolColumn =
        new TextColumn<RuntimeInfo>() {
          @Override
          public String getValue(RuntimeInfo record) {
            return valueOrDefault(record.getProtocol());
          }
        };

    Column<RuntimeInfo, SafeHtml> urlColumn =
        new Column<RuntimeInfo, SafeHtml>(
            new AbstractCell<SafeHtml>("click", "keydown") {

              @Override
              public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
                if (value != null) {
                  sb.append(value);
                }
              }

              @Override
              protected void onEnterKeyDown(
                  Context context,
                  Element parent,
                  SafeHtml value,
                  NativeEvent event,
                  ValueUpdater<SafeHtml> valueUpdater) {
                if (valueUpdater != null) {
                  valueUpdater.update(value);
                }
              }

              @Override
              public void onBrowserEvent(
                  Context context,
                  Element parent,
                  SafeHtml value,
                  NativeEvent event,
                  ValueUpdater<SafeHtml> valueUpdater) {
                super.onBrowserEvent(context, parent, value, event, valueUpdater);
                if ("click".equals(event.getType())) {
                  onEnterKeyDown(context, parent, value, event, valueUpdater);
                }
              }
            }) {
          @Override
          public SafeHtml getValue(RuntimeInfo record) {
            String value = valueOrDefault(record.getUrl());

            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            sb.appendHtmlConstant("<a target=\"_blank\" href=\"" + value + "\">");
            sb.appendEscaped(value);
            sb.appendHtmlConstant("</a>");
            return sb.toSafeHtml();
          }
        };

    table.addColumn(referenceColumn, locale.cellTableReferenceColumn());
    table.addColumn(portColumn, locale.cellTablePortColumn());
    table.addColumn(protocolColumn, locale.cellTableProtocolColumn());
    table.addColumn(urlColumn, locale.cellTableUrlColumn());

    table.setColumnWidth(referenceColumn, 10., Unit.PCT);
    table.setColumnWidth(portColumn, 10., Unit.PCT);
    table.setColumnWidth(protocolColumn, 10., Unit.PCT);
    table.setColumnWidth(urlColumn, 70., Unit.PCT);

    ListDataProvider<RuntimeInfo> dataProvider = new ListDataProvider<>(runtimeList);

    dataProvider.addDataDisplay(table);

    return table;
  }

  interface TableResources extends CellTable.Resources {

    @Source({Style.DEFAULT_CSS, "TableResources.css"})
    TableStyle cellTableStyle();
  }

  interface TableStyle extends CellTable.Style {
    String cellTableCaption();
  }
}
