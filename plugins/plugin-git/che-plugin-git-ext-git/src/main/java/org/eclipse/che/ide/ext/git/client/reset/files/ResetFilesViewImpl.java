/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.git.client.reset.files;

import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.api.git.shared.IndexFile;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ui.window.Window;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

import static com.google.gwt.user.client.ui.HasHorizontalAlignment.ALIGN_LEFT;

/**
 * The implementation of {@link ResetFilesPresenter}.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ResetFilesViewImpl extends Window implements ResetFilesView {
    interface ResetFilesViewImplUiBinder extends UiBinder<Widget, ResetFilesViewImpl> {
    }

    private static ResetFilesViewImplUiBinder ourUiBinder = GWT.create(ResetFilesViewImplUiBinder.class);

    Button btnReset;
    Button btnCancel;
    @UiField(provided = true)
    CellTable<IndexFile> indexFiles;
    @UiField(provided = true)
    final   GitLocalizationConstant locale;
    final   GitResources            resources;
    private ActionDelegate          delegate;

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

        btnCancel = createButton(locale.buttonCancel(), "git-resetFiles-btnCancel", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        addButtonToFooter(btnCancel);

        btnReset = createButton(locale.buttonReset(), "git-resetFiles-btnReset", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                delegate.onResetClicked();
            }
        });
        addButtonToFooter(btnReset);
    }

    /** Initialize the columns of the grid. */
    private void initColumns() {
        indexFiles = new CellTable<IndexFile>();

        // Create files column:
        Column<IndexFile, String> filesColumn = new Column<IndexFile, String>(new TextCell()) {
            @Override
            public String getValue(IndexFile file) {
                return file.getPath();
            }
        };

        // Create column with checkboxes:
        Column<IndexFile, Boolean> checkColumn = new Column<IndexFile, Boolean>(new CheckboxCell(false, true)) {
            @Override
            public Boolean getValue(IndexFile file) {
                return !file.isIndexed();
            }
        };

        // Create bean value updater:
        FieldUpdater<IndexFile, Boolean> checkFieldUpdater = new FieldUpdater<IndexFile, Boolean>() {
            @Override
            public void update(int index, IndexFile file, Boolean value) {
                file.setIndexed(!value);
            }
        };

        checkColumn.setFieldUpdater(checkFieldUpdater);

        filesColumn.setHorizontalAlignment(ALIGN_LEFT);

        indexFiles.addColumn(checkColumn, new SafeHtml() {
            @Override
            public String asString() {
                return "&nbsp;";
            }
        });
        indexFiles.setColumnWidth(checkColumn, 1, Style.Unit.PCT);
        indexFiles.addColumn(filesColumn, FILES);
        indexFiles.setColumnWidth(filesColumn, 35, Style.Unit.PCT);
        indexFiles.addStyleName(resources.gitCSS().cells());
    }

    @Override
    protected void onEnterClicked() {
        if (isWidgetFocused(btnCancel)) {
            delegate.onCancelClicked();
            return;
        }

        if (isWidgetFocused(btnReset)) {
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
