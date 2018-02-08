/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.compare.revisionslist;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
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
    onClose();
  }

  @Override
  protected void onClose() {
    selectionModel.clear();
    super.onClose();
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

    selectionModel = new SingleSelectionModel<Revision>();
    selectionModel.addSelectionChangeHandler(
        new SelectionChangeEvent.Handler() {
          @Override
          public void onSelectionChange(SelectionChangeEvent event) {
            description.setText(selectionModel.getSelectedObject().getMessage());
            delegate.onRevisionSelected(selectionModel.getSelectedObject());
          }
        });
    revisions.setSelectionModel(selectionModel);

    revisions.addDomHandler(
        new DoubleClickHandler() {
          @Override
          public void onDoubleClick(DoubleClickEvent event) {
            delegate.onRevisionDoubleClicked();
          }
        },
        DoubleClickEvent.getType());

    this.revisionsPanel.add(revisions);
  }

  private void createButtons() {
    btnClose =
        createButton(
            locale.buttonClose(),
            "git-compare-revision-close",
            new ClickHandler() {

              @Override
              public void onClick(ClickEvent event) {
                delegate.onCloseClicked();
              }
            });
    addButtonToFooter(btnClose);

    btnCompare =
        createButton(
            locale.buttonCompare(),
            "git-compare-revision-compare",
            new ClickHandler() {

              @Override
              public void onClick(ClickEvent event) {
                delegate.onCompareClicked();
              }
            });
    addButtonToFooter(btnCompare);
  }
}
