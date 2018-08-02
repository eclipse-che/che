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
package org.eclipse.che.ide.ext.git.client.revert;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.ide.ext.git.client.DateTimeFormatter;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of {@link RevertCommitView}.
 *
 * @author Dmitrii Bocharov (bdshadow)
 */
@Singleton
public class RevertCommitViewImpl extends Window implements RevertCommitView {
  interface RevertCommitViewImplUiBinder extends UiBinder<Widget, RevertCommitViewImpl> {}

  private static RevertCommitViewImplUiBinder uiBinder =
      GWT.create(RevertCommitViewImplUiBinder.class);

  @UiField ScrollPanel revisionsPanel;

  Button btnRevert;
  Button btnCancel;

  @UiField(provided = true)
  final GitResources res;

  @UiField(provided = true)
  final GitLocalizationConstant locale;

  private ActionDelegate delegate;
  private CellTable<Revision> revisions;
  private SingleSelectionModel<Revision> selectionModel;

  private final DateTimeFormatter dateTimeFormatter;

  @Inject
  protected RevertCommitViewImpl(
      GitResources resources,
      GitLocalizationConstant locale,
      org.eclipse.che.ide.Resources coreRes,
      DateTimeFormatter dateTimeFormatter) {
    this.res = resources;
    this.locale = locale;
    this.dateTimeFormatter = dateTimeFormatter;
    this.ensureDebugId("git-revert-window");

    Widget widget = uiBinder.createAndBindUi(this);

    this.setTitle(locale.revertCommitViewTitle());
    this.setWidget(widget);

    createRevisionsTable(coreRes);
    createButtons();
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void setRevisions(List<Revision> revisions) {
    this.revisions.setRowData(revisions);
  }

  @Override
  public void setEnableRevertButton(boolean enabled) {
    btnRevert.setEnabled(enabled);
  }

  @Override
  public void close() {
    hide();
  }

  @Override
  protected void onHide() {
    selectionModel.clear();
  }

  @Override
  public void showDialog() {
    this.show();
  }

  private void createRevisionsTable(org.eclipse.che.ide.Resources coreRes) {
    Column<Revision, String> idColumn =
        new Column<Revision, String>(new TextCell()) {
          @Override
          public String getValue(Revision revision) {
            return revision.getId().substring(0, 8);
          }
        };
    Column<Revision, String> dateColumn =
        new Column<Revision, String>(new TextCell()) {
          @Override
          public String getValue(Revision revision) {
            return dateTimeFormatter.getFormattedDate(revision.getCommitTime());
          }
        };
    Column<Revision, String> authorColumn =
        new Column<Revision, String>(new TextCell()) {
          @Override
          public String getValue(Revision revision) {
            return revision.getCommitter().getName();
          }
        };
    Column<Revision, String> commentColumn =
        new Column<Revision, String>(new TextCell()) {
          @Override
          public String getValue(Revision revision) {
            return revision.getMessage().substring(0, 50);
          }
        };
    revisions = new CellTable<>(15, coreRes);
    revisions.setWidth("100%");
    revisions.addColumn(idColumn, locale.viewRevertRevisionTableIdTitle());
    revisions.setColumnWidth(idColumn, "10%");
    revisions.addColumn(dateColumn, locale.viewRevertRevisionTableDateTitle());
    revisions.setColumnWidth(dateColumn, "20%");
    revisions.addColumn(authorColumn, locale.viewRevertRevisionTableAuthorTitle());
    revisions.setColumnWidth(authorColumn, "20%");
    revisions.addColumn(commentColumn, locale.viewRevertRevisionTableCommentTitle());
    revisions.setColumnWidth(commentColumn, "50%");

    this.selectionModel = new SingleSelectionModel<>();
    this.selectionModel.addSelectionChangeHandler(
        event -> {
          Revision selectedObject = selectionModel.getSelectedObject();
          delegate.onRevisionSelected(selectedObject);
        });
    revisions.setSelectionModel(this.selectionModel);
    this.revisionsPanel.add(revisions);
  }

  private void createButtons() {
    btnCancel =
        addFooterButton(
            locale.buttonCancel(), "git-revert-cancel", event -> delegate.onCancelClicked());

    btnRevert =
        addFooterButton(
            locale.buttonRevert(), "git-revert", event -> delegate.onRevertClicked(), true);
  }

  @UiHandler("revisionsPanel")
  public void onPanelScrolled(ScrollEvent ignored) {
    // We cannot rely on exact equality of scroll positions because GWT sometimes round such values
    // and it is possible that the actual max scroll position is a pixel less then declared.
    if (revisionsPanel.getMaximumVerticalScrollPosition()
            - revisionsPanel.getVerticalScrollPosition()
        <= 1) {
      // to avoid autoscrolling to selected item
      revisionsPanel.getElement().focus();

      delegate.onScrolledToBottom();
    }
  }
}
