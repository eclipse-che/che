/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.macro.chooser;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of the {@link MacroChooserView} that shows table for exploring and choosing
 * macros. Also provides ability to filter data in the table.
 *
 * @author Artem Zatsarynnyi
 */
public class MacroChooserViewImpl extends Window implements MacroChooserView {

  private static final MacroChooserViewImplUiBinder UI_BINDER =
      GWT.create(MacroChooserViewImplUiBinder.class);

  @UiField(provided = true)
  CellTable<Macro> macrosTable;

  @UiField TextBox filterField;

  private ActionDelegate delegate;

  @Inject
  public MacroChooserViewImpl(org.eclipse.che.ide.Resources resources) {
    setTitle("Command Macros");

    initMacrosTable(resources);

    setWidget(UI_BINDER.createAndBindUi(this));

    filterField.getElement().setAttribute("placeholder", "Search macro");
  }

  private void initMacrosTable(org.eclipse.che.ide.Resources resources) {
    macrosTable = new CellTable<>(500, resources);

    final Column<Macro, String> nameColumn =
        new Column<Macro, String>(new TextCell()) {
          @Override
          public String getValue(Macro remote) {
            return remote.getName();
          }
        };

    final Column<Macro, String> descriptionColumn =
        new Column<Macro, String>(new TextCell()) {
          @Override
          public String getValue(Macro remote) {
            return remote.getDescription();
          }
        };

    macrosTable.addColumn(nameColumn, "Macro");
    macrosTable.setColumnWidth(nameColumn, "40%");
    macrosTable.addColumn(descriptionColumn, "Description");
    macrosTable.setColumnWidth(descriptionColumn, "60%");

    final SingleSelectionModel<Macro> selectionModel = new SingleSelectionModel<>();

    macrosTable.setSelectionModel(selectionModel);

    macrosTable.addDomHandler(
        event -> {
          if (selectionModel.getSelectedObject() != null) {
            delegate.onMacroChosen(selectionModel.getSelectedObject());
          }
        },
        DoubleClickEvent.getType());

    macrosTable.addDomHandler(
        event -> {
          if (selectionModel.getSelectedObject() != null
              && (KeyCodes.KEY_ENTER == event.getNativeKeyCode()
                  || KeyCodes.KEY_MAC_ENTER == event.getNativeKeyCode())) {

            delegate.onMacroChosen(selectionModel.getSelectedObject());
          }
        },
        KeyUpEvent.getType());
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void showDialog() {
    show(filterField);
  }

  @Override
  protected void onShow() {
    filterField.setValue("");
  }

  @Override
  public void close() {
    hide();
  }

  @Override
  public void bindMacrosList(ListDataProvider<Macro> dataProvider) {
    dataProvider.addDataDisplay(macrosTable);
  }

  @UiHandler({"filterField"})
  void onFilterChanged(@SuppressWarnings("UnusedParameters") KeyUpEvent event) {
    delegate.onFilterChanged(filterField.getValue());
  }

  interface MacroChooserViewImplUiBinder extends UiBinder<Widget, MacroChooserViewImpl> {}
}
