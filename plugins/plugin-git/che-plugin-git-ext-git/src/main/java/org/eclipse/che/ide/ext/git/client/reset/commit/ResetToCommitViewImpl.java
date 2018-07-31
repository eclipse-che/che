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
package org.eclipse.che.ide.ext.git.client.reset.commit;

import static org.eclipse.che.ide.util.dom.DomUtils.isWidgetOrChildFocused;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of {@link ResetToCommitView}.
 *
 * @author Andrey Plotnikov
 */
@Singleton
public class ResetToCommitViewImpl extends Window implements ResetToCommitView {
  interface ResetToCommitViewImplUiBinder extends UiBinder<Widget, ResetToCommitViewImpl> {}

  private static ResetToCommitViewImplUiBinder ourUiBinder =
      GWT.create(ResetToCommitViewImplUiBinder.class);

  @UiField RadioButton soft;
  @UiField RadioButton mixed;
  @UiField RadioButton hard;
  Button btnReset;
  Button btnCancel;
  @UiField ScrollPanel revisionsPanel;

  @UiField(provided = true)
  CellTable<Revision> commits;

  @UiField(provided = true)
  final GitResources res;

  @UiField(provided = true)
  final GitLocalizationConstant locale;

  private ActionDelegate delegate;

  /**
   * Create view.
   *
   * @param resources
   * @param locale
   */
  @Inject
  protected ResetToCommitViewImpl(GitResources resources, GitLocalizationConstant locale) {
    this.res = resources;
    this.locale = locale;
    this.ensureDebugId("git-reset-window");

    createCommitsTable();

    Widget widget = ourUiBinder.createAndBindUi(this);

    this.setTitle(locale.resetCommitViewTitle());
    this.setWidget(widget);

    prepareRadioButtons();

    btnCancel =
        addFooterButton(
            locale.buttonCancel(), "git-reset-cancel", event -> delegate.onCancelClicked());

    btnReset =
        addFooterButton(
            locale.buttonReset(), "git-reset-reset", event -> delegate.onResetClicked(), true);
  }

  @Override
  public void onEnterPress(NativeEvent evt) {
    if (isWidgetOrChildFocused(btnCancel)) {
      delegate.onCancelClicked();
    } else if (isWidgetOrChildFocused(btnReset)) {
      delegate.onResetClicked();
    }
  }

  /** Add description to buttons. */
  private void prepareRadioButtons() {
    addDescription(soft, locale.resetSoftTypeDescription());
    addDescription(mixed, locale.resetMixedTypeDescription());
    addDescription(hard, locale.resetHardTypeDescription());
  }

  /**
   * Add description to radio button title.
   *
   * @param radioItem radio button
   * @param description description to add
   */
  private void addDescription(RadioButton radioItem, String description) {
    Element descElement = DOM.createSpan();
    descElement.setInnerText(" " + description);
    descElement.getStyle().setColor("#888");
    radioItem.getElement().appendChild(descElement);
  }

  public interface TableRes extends CellTable.Resources {
    @Source({
      CellTable.Style.DEFAULT_CSS,
      "org/eclipse/che/ide/ext/git/client/reset/commit/custom.css"
    })
    TableStyle cellTableStyle();

    interface TableStyle extends CellTable.Style {}
  }

  private CellTable.Resources tableRes = GWT.create(TableRes.class);

  /** Creates table what contains list of available commits. */
  private void createCommitsTable() {
    commits = new CellTable<>(15, tableRes);

    Column<Revision, String> dateColumn =
        new Column<Revision, String>(new TextCell()) {
          @Override
          public String getValue(Revision revision) {
            return DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM)
                .format(new Date(revision.getCommitTime()));
          }

          @Override
          public void render(Cell.Context context, Revision revision, SafeHtmlBuilder sb) {
            sb.appendHtmlConstant(
                "<div id=\""
                    + UIObject.DEBUG_ID_PREFIX
                    + "git-reset-cellTable-"
                    + context.getIndex()
                    + "\">");
            super.render(context, revision, sb);
          }
        };
    Column<Revision, String> commiterColumn =
        new Column<Revision, String>(new TextCell()) {
          @Override
          public String getValue(Revision revision) {
            if (revision.getCommitter() == null) {
              return "";
            }
            return revision.getCommitter().getName();
          }
        };
    Column<Revision, String> commentColumn =
        new Column<Revision, String>(new TextCell()) {
          @Override
          public String getValue(Revision revision) {
            return revision.getMessage();
          }
        };

    commits.addColumn(dateColumn, locale.commitGridDate());
    commits.setColumnWidth(dateColumn, "20%");
    commits.addColumn(commiterColumn, locale.commitGridCommiter());
    commits.setColumnWidth(commiterColumn, "20%");
    commits.addColumn(commentColumn, locale.commitGridComment());
    commits.setColumnWidth(commentColumn, "60%");

    final SingleSelectionModel<Revision> selectionModel = new SingleSelectionModel<Revision>();
    selectionModel.addSelectionChangeHandler(
        event -> {
          Revision selectedObject = selectionModel.getSelectedObject();
          delegate.onRevisionSelected(selectedObject);
        });
    commits.setSelectionModel(selectionModel);
  }

  /** {@inheritDoc} */
  @Override
  public void setRevisions(@NotNull List<Revision> revisions) {
    // Wraps Array in java.util.List
    List<Revision> list = new ArrayList<>();
    list.addAll(revisions);

    this.commits.setRowData(list);
  }

  @Override
  public void resetRevisionSelection() {
    ((SingleSelectionModel) commits.getSelectionModel()).clear();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSoftMode() {
    return soft.getValue();
  }

  /** {@inheritDoc} */
  @Override
  public void setSoftMode(boolean isSoft) {
    soft.setValue(isSoft);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isMixMode() {
    return mixed.getValue();
  }

  /** {@inheritDoc} */
  @Override
  public void setMixMode(boolean isMix) {
    mixed.setValue(isMix);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isHardMode() {
    return hard.getValue();
  }

  /** {@inheritDoc} */
  @Override
  public void setHardMode(boolean isHard) {
    hard.setValue(isHard);
  }

  /** {@inheritDoc} */
  @Override
  public void setEnableResetButton(final boolean enabled) {
    btnReset.setEnabled(enabled);
    Scheduler.get().scheduleDeferred(() -> btnReset.setFocus(enabled));
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    this.hide();
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    this.show();
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
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
