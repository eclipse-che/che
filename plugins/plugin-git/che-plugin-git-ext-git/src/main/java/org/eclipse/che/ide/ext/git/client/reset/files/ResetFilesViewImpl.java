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
package org.eclipse.che.ide.ext.git.client.reset.files;

import static com.google.gwt.user.client.ui.HasHorizontalAlignment.ALIGN_LEFT;
import static org.eclipse.che.ide.util.dom.DomUtils.isWidgetOrChildFocused;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.git.shared.IndexFile;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of {@link ResetFilesPresenter}.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ResetFilesViewImpl extends Window implements ResetFilesView {
  interface ResetFilesViewImplUiBinder extends UiBinder<Widget, ResetFilesViewImpl> {}

  private static ResetFilesViewImplUiBinder ourUiBinder =
      GWT.create(ResetFilesViewImplUiBinder.class);

  Button btnReset;
  Button btnCancel;

  @UiField(provided = true)
  CellTable<IndexFile> indexFiles;

  @UiField(provided = true)
  final GitLocalizationConstant locale;

  final GitResources resources;
  private ActionDelegate delegate;

  /**
   * Create view.
   *
   * @param locale
   */
  @Inject
  protected ResetFilesViewImpl(GitLocalizationConstant locale, GitResources resources) {
    this.locale = locale;
    this.resources = resources;
    this.ensureDebugId("git-resetFiles-window");

    initColumns();

    Widget widget = ourUiBinder.createAndBindUi(this);

    this.setTitle(locale.resetFilesViewTitle());
    this.setWidget(widget);

    btnCancel =
        addFooterButton(
            locale.buttonCancel(), "git-resetFiles-btnCancel", event -> delegate.onCancelClicked());

    btnReset =
        addFooterButton(
            locale.buttonReset(),
            "git-resetFiles-btnReset",
            event -> delegate.onResetClicked(),
            true);
  }

  /** Initialize the columns of the grid. */
  private void initColumns() {
    indexFiles = new CellTable<>();

    // Create files column:
    Column<IndexFile, String> filesColumn =
        new Column<IndexFile, String>(new TextCell()) {
          @Override
          public String getValue(IndexFile file) {
            return file.getPath();
          }
        };

    // Create column with checkboxes:
    Column<IndexFile, Boolean> checkColumn =
        new Column<IndexFile, Boolean>(new CheckboxCell(false, true)) {
          @Override
          public Boolean getValue(IndexFile file) {
            return !file.isIndexed();
          }
        };

    // Create bean value updater:
    FieldUpdater<IndexFile, Boolean> checkFieldUpdater =
        (index, file, value) -> file.setIndexed(!value);

    checkColumn.setFieldUpdater(checkFieldUpdater);

    filesColumn.setHorizontalAlignment(ALIGN_LEFT);

    indexFiles.addColumn(checkColumn, (SafeHtml) () -> "&nbsp;");
    indexFiles.setColumnWidth(checkColumn, 1, Style.Unit.PCT);
    indexFiles.addColumn(filesColumn, FILES);
    indexFiles.setColumnWidth(filesColumn, 35, Style.Unit.PCT);
    indexFiles.addStyleName(resources.gitCSS().cells());
  }

  @Override
  public void onEnterPress(NativeEvent evt) {
    if (isWidgetOrChildFocused(btnCancel)) {
      delegate.onCancelClicked();
    } else if (isWidgetOrChildFocused(btnReset)) {
      delegate.onResetClicked();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setIndexedFiles(IndexFile[] indexedFiles) {
    List<IndexFile> appList = new ArrayList<>();
    Collections.addAll(appList, indexedFiles);
    indexFiles.setRowData(appList);
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    this.hide();
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    this.show(btnReset);
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }
}
