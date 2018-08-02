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
package org.eclipse.che.ide.ext.git.client.compare.revisionslist;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.ide.ext.git.client.DateTimeFormatter;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of {@link RevisionListView}.
 *
 * @author Igor Vinokur
 */
@Singleton
public class RevisionListViewImpl extends Window implements RevisionListView {
  interface RevisionListViewImplUiBinder extends UiBinder<Widget, RevisionListViewImpl> {}

  private static RevisionListViewImplUiBinder uiBinder =
      GWT.create(RevisionListViewImplUiBinder.class);

  Button btnClose;
  Button btnCompare;

  @UiField ScrollPanel revisionsPanel;
  @UiField TextArea description;

  @UiField(provided = true)
  final GitLocalizationConstant locale;

  @UiField(provided = true)
  final GitResources res;

  private ActionDelegate delegate;
  private CellTable<Revision> revisions;
  private SingleSelectionModel<Revision> selectionModel;

  private final DateTimeFormatter dateTimeFormatter;

  @Inject
  protected RevisionListViewImpl(
      GitResources resources,
      GitLocalizationConstant locale,
      DateTimeFormatter dateTimeFormatter,
      org.eclipse.che.ide.Resources coreRes) {
    this.res = resources;
    this.locale = locale;
    this.dateTimeFormatter = dateTimeFormatter;
    this.ensureDebugId("git-compare-revision-window");

    Widget widget = uiBinder.createAndBindUi(this);

    this.setTitle(locale.compareWithRevisionTitle());
    this.setWidget(widget);

    description.setReadOnly(true);

    createRevisionsTable(coreRes);
    createButtons();
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  public void setRevisions(@NotNull List<Revision> revisions) {
    this.revisions.setRowData(revisions);
    if (selectionModel.getSelectedObject() == null) {
      delegate.onRevisionUnselected();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setEnableCompareButton(boolean enabled) {
    btnCompare.setEnabled(enabled);
  }

  /** {@inheritDoc} */
  @Override
  public void setDescription(String description) {
    this.description.setText(description);
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    hide();
  }

  @Override
  protected void onHide() {
    selectionModel.clear();
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    this.show();
  }

  private void createRevisionsTable(org.eclipse.che.ide.Resources coreRes) {
    Column<Revision, String> idColumn =
        new Column<Revision, String>(new TextCell()) {
          @Override
          public String getValue(Revision revision) {
            return revision.getId().substring(0, 8) + "...";
          }
        };
    Column<Revision, String> timeColumn =
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
    Column<Revision, String> titleColumn =
        new Column<Revision, String>(new TextCell()) {
          @Override
          public String getValue(Revision revision) {
            return revision.getMessage().substring(0, 50);
          }
        };

    revisions = new CellTable<>(15, coreRes);

    revisions.setWidth("100%");

    revisions.addColumn(idColumn, locale.viewCompareRevisionTableIdTitle());
    revisions.addColumn(timeColumn, locale.viewCompareRevisionTableTimeTitle());
    revisions.addColumn(authorColumn, locale.viewCompareRevisionTableAuthorTitle());
    revisions.addColumn(titleColumn, locale.viewCompareRevisionTableTitleTitle());

    selectionModel = new SingleSelectionModel<>();
    selectionModel.addSelectionChangeHandler(
        event -> {
          description.setText(selectionModel.getSelectedObject().getMessage());
          delegate.onRevisionSelected(selectionModel.getSelectedObject());
        });
    revisions.setSelectionModel(selectionModel);

    revisions.addDomHandler(
        event -> delegate.onRevisionDoubleClicked(), DoubleClickEvent.getType());

    this.revisionsPanel.add(revisions);
  }

  private void createButtons() {
    btnClose =
        addFooterButton(
            locale.buttonClose(), "git-compare-revision-close", event -> delegate.onCloseClicked());

    btnCompare =
        addFooterButton(
            locale.buttonCompare(),
            "git-compare-revision-compare",
            event -> delegate.onCompareClicked());
  }
}
