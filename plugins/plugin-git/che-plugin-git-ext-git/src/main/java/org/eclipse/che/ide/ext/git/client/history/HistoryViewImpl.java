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
package org.eclipse.che.ide.ext.git.client.history;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.git.shared.Constants;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.vectomatic.dom.svg.ui.SVGImage;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * The implementation of {@link HistoryView}.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
@Singleton
public class HistoryViewImpl extends BaseView<HistoryView.ActionDelegate> implements HistoryView {

    interface HistoryViewImplUiBinder extends UiBinder<Widget, HistoryViewImpl> {
    }

    @UiField
    DockLayoutPanel     dataCommitBPanel;
    @UiField
    DockLayoutPanel     revisionCommitBPanel;
    @UiField
    HTML                compareType;
    @UiField
    TextBox             commitARevision;
    @UiField
    TextBox             commitADate;
    @UiField
    TextBox             commitBRevision;
    @UiField
    TextBox             commitBDate;
    @UiField
    TextArea            editor;
    @UiField(provided = true)
    CellTable<Revision> commits;
    @UiField
    Button              btnRefresh;
    @UiField
    Button              btnProjectChanges;
    @UiField
    Button              btnResourceChanges;
    @UiField
    Button              btnDiffWithIndex;
    @UiField
    Button              btnDiffWithWorkTree;
    @UiField
    Button              btnDiffWithPrevCommit;
    @UiField
    ScrollPanel         scrollPanel;
    @UiField(provided = true)
    final GitResources            res;
    @UiField(provided = true)
    final GitLocalizationConstant locale;

    /**
     * Create view.
     */
    @Inject
    protected HistoryViewImpl(final GitResources resources,
                              final GitLocalizationConstant locale,
                              final PartStackUIResources partStackUIResources,
                              final Resources res,
                              final HistoryViewImplUiBinder uiBinder) {
        super(partStackUIResources);

        this.res = resources;
        this.locale = locale;

        createCommitsTable(res);
        setContentWidget(uiBinder.createAndBindUi(this));
        minimizeButton.ensureDebugId("git-showHistory-minimizeBut");
        this.scrollPanel.getElement().setTabIndex(-1);

        btnProjectChanges.getElement().appendChild(new SVGImage(resources.projectLevel()).getElement());
        btnResourceChanges.getElement().appendChild(new SVGImage(resources.resourceLevel()).getElement());
        btnDiffWithIndex.getElement().appendChild(new SVGImage(resources.diffIndex()).getElement());
        btnDiffWithWorkTree.getElement().appendChild(new SVGImage(resources.diffWorkTree()).getElement());
        btnDiffWithPrevCommit.getElement().appendChild(new SVGImage(resources.diffPrevVersion()).getElement());
        btnRefresh.getElement().appendChild(new SVGImage(resources.refresh()).getElement());
    }

    /** Creates table what contains list of available commits. */
    private void createCommitsTable(Resources res) {
        commits = new CellTable<Revision>(Constants.DEFAULT_PAGE_SIZE, res);
        commits.setRowData(Collections.<Revision>emptyList());

        Column<Revision, String> dateColumn = new Column<Revision, String>(new TextCell()) {
            @Override
            public String getValue(Revision revision) {
                return DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM).format(
                        new Date((long)revision.getCommitTime()));
            }

            @Override
            public void render(Cell.Context context, Revision revision, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div id=\"" + UIObject.DEBUG_ID_PREFIX + "git-showHistory-table-" + context.getIndex() + "\">");
                super.render(context, revision, sb);
            }
        };
        Column<Revision, String> commiterColumn = new Column<Revision, String>(new TextCell()) {
            @Override
            public String getValue(Revision revision) {
                if (revision.getCommitter() == null) {
                    return "";
                }
                return revision.getCommitter().getName();
            }

        };
        Column<Revision, String> commentColumn = new Column<Revision, String>(new TextCell()) {
            @Override
            public String getValue(Revision revision) {
                return revision.getMessage();
            }
        };

        commits.addColumn(dateColumn, locale.commitGridDate());
        commits.setColumnWidth(dateColumn, "20%");
        commits.addColumn(commiterColumn, locale.commitGridCommiter());
        commits.setColumnWidth(commiterColumn, "30%");
        commits.addColumn(commentColumn, locale.commitGridComment());
        commits.setColumnWidth(commentColumn, "50%");

        final SingleSelectionModel<Revision> selectionModel = new SingleSelectionModel<Revision>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Revision selectedObject = selectionModel.getSelectedObject();
                delegate.onRevisionSelected(selectedObject);
            }
        });
        commits.setSelectionModel(selectionModel);
    }

    /** {@inheritDoc} */
    @Override
    public void setRevisions(@NotNull List<Revision> revisions) {
        commits.setRowData(revisions);

        // if the size of the panel is greater then the size of the loaded list of the history then no scroller has been appeared yet
        onPanelScrolled(null);
    }

    /** {@inheritDoc} */
    @Override
    public void selectProjectChangesButton(boolean selected) {
        btnProjectChanges.setEnabled(!selected);
    }

    /** {@inheritDoc} */
    @Override
    public void selectResourceChangesButton(boolean selected) {
        btnResourceChanges.setEnabled(!selected);
    }

    /** {@inheritDoc} */
    @Override
    public void selectDiffWithIndexButton(boolean selected) {
        btnDiffWithIndex.setEnabled(!selected);
    }

    /** {@inheritDoc} */
    @Override
    public void selectDiffWithWorkingTreeButton(boolean selected) {
        btnDiffWithWorkTree.setEnabled(!selected);
    }

    /** {@inheritDoc} */
    @Override
    public void selectDiffWithPrevVersionButton(boolean selected) {
        btnDiffWithPrevCommit.setEnabled(!selected);
    }

    /** {@inheritDoc} */
    @Override
    public void setCommitADate(@NotNull String date) {
        commitADate.setText(date);
    }

    /** {@inheritDoc} */
    @Override
    public void setCommitBDate(@NotNull String date) {
        commitBDate.setText(date);
    }

    /** {@inheritDoc} */
    @Override
    public void setCommitARevision(@NotNull String revision) {
        commitARevision.setText(revision);
    }

    /** {@inheritDoc} */
    @Override
    public void setCommitBRevision(@NotNull String revision) {
        commitBRevision.setText(revision);
    }

    /** {@inheritDoc} */
    @Override
    public void setCompareType(@NotNull String type) {
        compareType.setHTML(type);
    }

    /** {@inheritDoc} */
    @Override
    public void setDiffContext(@NotNull String diffContext) {
        editor.setText(diffContext);
    }

    /** {@inheritDoc} */
    @Override
    public void setCommitBPanelVisible(boolean visible) {
        revisionCommitBPanel.setVisible(visible);
        dataCommitBPanel.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        dataCommitBPanel.clear();
        revisionCommitBPanel.clear();
        commitARevision.setText("");
        commitBRevision.setText("");
        commitADate.setText("");
        commitBDate.setText("");
        compareType.setText("");

        setDiffContext("");

        List<Revision> list = new ArrayList<Revision>();
        commits.setRowData(list);
    }

    @UiHandler("btnRefresh")
    public void onRefreshClicked(ClickEvent event) {
        delegate.onRefreshClicked();
    }

    @UiHandler("btnProjectChanges")
    public void onProjectChangesClick(ClickEvent event) {
        delegate.onProjectChangesClicked();
    }

    @UiHandler("btnResourceChanges")
    public void onResourceChangesClicked(ClickEvent event) {
        delegate.onResourceChangesClicked();
    }

    @UiHandler("btnDiffWithIndex")
    public void onDiffWithIndexClicked(ClickEvent event) {
        delegate.onDiffWithIndexClicked();
    }

    @UiHandler("btnDiffWithWorkTree")
    public void onDiffWithWorkTreeClicked(ClickEvent event) {
        delegate.onDiffWithWorkTreeClicked();
    }

    @UiHandler("btnDiffWithPrevCommit")
    public void onDiffWithPrevCommitClicked(ClickEvent event) {
        delegate.onDiffWithPrevCommitClicked();
    }

    @UiHandler("scrollPanel")
    public void onPanelScrolled(ScrollEvent event) {
        if (scrollPanel.getVerticalScrollPosition() == scrollPanel.getMaximumVerticalScrollPosition()) {
            // to avoid autoscrolling to selected item
            scrollPanel.getElement().focus();

            delegate.onScrolledToButton();
        }
    }
}
