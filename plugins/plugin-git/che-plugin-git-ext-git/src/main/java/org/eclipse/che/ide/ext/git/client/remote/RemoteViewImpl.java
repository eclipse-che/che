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
package org.eclipse.che.ide.ext.git.client.remote;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of {@link RemoteView}.
 *
 * @author Andrey Plotnikov
 */
@Singleton
public class RemoteViewImpl extends Window implements RemoteView {
  interface RemoteViewImplUiBinder extends UiBinder<Widget, RemoteViewImpl> {}

  private static RemoteViewImplUiBinder ourUiBinder = GWT.create(RemoteViewImplUiBinder.class);

  Button btnClose;
  Button btnAdd;
  Button btnDelete;

  @UiField(provided = true)
  CellTable<Remote> repositories;

  private Remote selectedObject;

  @UiField(provided = true)
  final GitResources res;

  @UiField(provided = true)
  final GitLocalizationConstant locale;

  private ActionDelegate delegate;
  private boolean isShown;
  private final DialogFactory dialogFactory;

  /** Create view. */
  @Inject
  protected RemoteViewImpl(
      GitResources resources,
      final GitLocalizationConstant locale,
      org.eclipse.che.ide.Resources ideResources,
      final DialogFactory dialogFactory) {
    this.res = resources;
    this.locale = locale;
    this.dialogFactory = dialogFactory;
    this.ensureDebugId("git-remotes-remotes-window");

    initRepositoriesTable(ideResources);

    Widget widget = ourUiBinder.createAndBindUi(this);

    this.setTitle(locale.remotesViewTitle());
    this.setWidget(widget);

    btnClose =
        createButton(
            locale.buttonClose(),
            "git-remotes-remotes-close",
            new ClickHandler() {

              @Override
              public void onClick(ClickEvent event) {
                delegate.onCloseClicked();
              }
            });
    addButtonToFooter(btnClose);

    btnAdd =
        createButton(
            locale.buttonAdd(),
            "git-remotes-remotes-add",
            new ClickHandler() {

              @Override
              public void onClick(ClickEvent event) {
                delegate.onAddClicked();
              }
            });
    addButtonToFooter(btnAdd);

    btnDelete =
        createButton(
            locale.buttonRemove(),
            "git-remotes-remotes-remove",
            new ClickHandler() {

              @Override
              public void onClick(ClickEvent event) {
                onDeleteClicked();
              }
            });
    addButtonToFooter(btnDelete);
  }

  @Override
  protected void onEnterClicked() {
    if (isWidgetFocused(btnClose)) {
      delegate.onCloseClicked();
      return;
    }

    if (isWidgetFocused(btnAdd)) {
      delegate.onAddClicked();
      return;
    }

    if (isWidgetFocused(btnDelete)) {
      onDeleteClicked();
    }
  }

  private void onDeleteClicked() {
    dialogFactory
        .createConfirmDialog(
            locale.deleteRemoteRepositoryTitle(),
            locale.deleteRemoteRepositoryQuestion(selectedObject.getName()),
            new ConfirmCallback() {
              @Override
              public void accepted() {
                delegate.onDeleteClicked();
              }
            },
            null)
        .show();
  }

  /**
   * Initialize the columns of the grid.
   *
   * @param ideResources
   */
  private void initRepositoriesTable(org.eclipse.che.ide.Resources ideResources) {
    repositories = new CellTable<>(15, ideResources);

    Column<Remote, String> nameColumn =
        new Column<Remote, String>(new TextCell()) {
          @Override
          public String getValue(Remote remote) {
            return remote.getName();
          }

          @Override
          public void render(Cell.Context context, Remote remote, SafeHtmlBuilder sb) {
            sb.appendHtmlConstant(
                "<div id=\""
                    + UIObject.DEBUG_ID_PREFIX
                    + "git-remotes-remotes-cellTable-"
                    + context.getIndex()
                    + "\">");
            super.render(context, remote, sb);
          }
        };
    Column<Remote, String> urlColumn =
        new Column<Remote, String>(new TextCell()) {
          @Override
          public String getValue(Remote remote) {
            return remote.getUrl();
          }
        };

    repositories.addColumn(nameColumn, locale.remoteGridNameField());
    repositories.setColumnWidth(nameColumn, "20%");
    repositories.addColumn(urlColumn, locale.remoteGridLocationField());
    repositories.setColumnWidth(urlColumn, "80%");

    final SingleSelectionModel<Remote> selectionModel = new SingleSelectionModel<Remote>();
    selectionModel.addSelectionChangeHandler(
        new SelectionChangeEvent.Handler() {
          @Override
          public void onSelectionChange(SelectionChangeEvent event) {
            selectedObject = selectionModel.getSelectedObject();
            delegate.onRemoteSelected(selectedObject);
          }
        });
    repositories.setSelectionModel(selectionModel);
  }

  /** {@inheritDoc} */
  @Override
  public void setRemotes(@NotNull List<Remote> remotes) {
    // Wraps Array in java.util.List
    List<Remote> list = new ArrayList<>();
    for (Remote remote : remotes) {
      list.add(remote);
    }
    repositories.setRowData(list);
  }

  /** {@inheritDoc} */
  @Override
  public void setEnableDeleteButton(boolean enabled) {
    btnDelete.setEnabled(enabled);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isShown() {
    return isShown;
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    this.isShown = false;
    this.hide();
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    this.isShown = true;
    this.show(btnAdd);
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  protected void onClose() {
    this.isShown = false;
    super.onClose();
  }
}
