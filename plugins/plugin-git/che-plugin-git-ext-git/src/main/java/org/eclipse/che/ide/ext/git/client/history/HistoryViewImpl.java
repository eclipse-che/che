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
package org.eclipse.che.ide.ext.git.client.history;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
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
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.ide.ext.git.client.DateTimeFormatter;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of {@link HistoryView}.
 *
 * @author Igor Vinokur
 */
@Singleton
public class HistoryViewImpl extends Window implements HistoryView {
  interface HistoryListViewImplUiBinder extends UiBinder<Widget, HistoryViewImpl> {}

  private static HistoryListViewImplUiBinder uiBinder =
      GWT.create(HistoryListViewImplUiBinder.class);

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
  protected HistoryViewImpl(
      GitResources resources,
      GitLocalizationConstant locale,
      DateTimeFormatter dateTimeFormatter,
      org.eclipse.che.ide.Resources coreRes) {
    this.res = resources;
    this.locale = locale;
    this.dateTimeFormatter = dateTimeFormatter;
    this.ensureDebugId("git-history-window");

    Widget widget = uiBinder.createAndBindUi(this);

    this.setTitle(locale.historyTitle());
    this.setWidget(widget);

    revisionsPanel.getElement().setTabIndex(-1);
    description.setReadOnly(true);

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
    if (selectionModel.getSelectedObject() == null) {
      delegate.onRevisionUnselected();
    }
    // if the size of the panel is greater then the size of the loaded list of the history then no
    // scroller has been appeared yet
    onPanelScrolled(null);
  }

  @Override
  public void setEnableCompareButton(boolean enabled) {
    btnCompare.setEnabled(enabled);
  }

  @Override
  public void setDescription(String description) {
    this.description.setText(description);
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
            return revision.getAuthor().getName();
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
            locale.buttonClose(), "git-history-close", event -> delegate.onCloseClicked());

    btnCompare =
        addFooterButton(
            locale.buttonCompare(),
            "git-history-compare",
            event -> delegate.onCompareClicked(),
            true);
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

      delegate.onScrolledToButton();
    }
  }
}
