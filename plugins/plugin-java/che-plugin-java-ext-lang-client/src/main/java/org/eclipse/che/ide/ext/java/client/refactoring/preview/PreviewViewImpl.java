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
package org.eclipse.che.ide.ext.java.client.refactoring.preview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.api.theme.ThemeAgent;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangePreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringPreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatusEntry;
import org.eclipse.che.ide.orion.compare.CompareConfig;
import org.eclipse.che.ide.orion.compare.CompareFactory;
import org.eclipse.che.ide.orion.compare.CompareWidget;
import org.eclipse.che.ide.orion.compare.FileOptions;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.window.Window;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.gwt.dom.client.Style.Float.LEFT;
import static com.google.gwt.dom.client.Style.Unit.PX;
import static org.eclipse.che.ide.api.theme.Style.getEditorSelectionColor;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
final class PreviewViewImpl extends Window implements PreviewView {

    interface PreviewViewImplUiBinder extends UiBinder<Widget, PreviewViewImpl> {
    }

    private static final PreviewViewImplUiBinder UI_BINDER = GWT.create(PreviewViewImplUiBinder.class);

    @UiField(provided = true)
    final JavaLocalizationConstant locale;
    @UiField
    SimplePanel   diff;
    @UiField
    FlowPanel     diffPanelToHide;
    @UiField
    SimplePanel   noPreviewToHide;
    @UiField
    VerticalPanel treePanel;
    @UiField
    Label         errorLabel;

    private ActionDelegate delegate;
    private FileOptions    newFile;
    private FileOptions    oldFile;
    private CompareWidget  compare;
    private ThemeAgent     themeAgent;

    private final LoaderFactory  loaderFactory;
    private final CompareFactory compareFactory;

    private Map<TreeItem, RefactoringPreview> containerChanges = new HashMap<>();
    private Element selectedElement;

    @Inject
    public PreviewViewImpl(JavaLocalizationConstant locale,
                           LoaderFactory loaderFactory,
                           CompareFactory compareFactory, ThemeAgent themeAgent) {
        this.locale = locale;
        this.loaderFactory = loaderFactory;
        this.compareFactory = compareFactory;
        this.themeAgent = themeAgent;

        setTitle(locale.moveDialogTitle());

        setWidget(UI_BINDER.createAndBindUi(this));

        Button backButton = createButton(locale.moveDialogButtonBack(), "preview-back-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onBackButtonClicked();
            }
        });

        Button cancelButton = createButton(locale.moveDialogButtonCancel(), "preview-cancel-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
                delegate.onCancelButtonClicked();
            }
        });

        Button acceptButton = createButton(locale.moveDialogButtonOk(), "preview-ok-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onAcceptButtonClicked();
            }
        });

        addButtonToFooter(acceptButton);
        addButtonToFooter(cancelButton);
        addButtonToFooter(backButton);

        diff.getElement().setId(Document.get().createUniqueId());
        showDiff(null);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void show() {
        errorLabel.setText("");
        diff.clear();
        compare = null;
        treePanel.clear();
        containerChanges.clear();

        super.show();
    }

    /** {@inheritDoc} */
    @Override
    protected void onClose() {
        delegate.onCancelButtonClicked();

        super.onClose();
    }

    /** {@inheritDoc} */
    @Override
    public void setTreeOfChanges(final RefactoringPreview changes) {
        showDiffPanel(false);

        final SelectionModel<RefactoringPreview> selectionModel = new SingleSelectionModel<>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                RefactoringPreview selectedNode = (RefactoringPreview)((SingleSelectionModel)selectionModel).getSelectedObject();
                delegate.onSelectionChanged(selectedNode);
            }
        });

        Tree tree = new Tree();

        tree.getElement().setId("tree-of-changes");

        for (RefactoringPreview parentChange : changes.getChildrens()) {
            TreeItem treeItem = new TreeItem();
            containerChanges.put(treeItem, parentChange);
            createTreeElement(treeItem, parentChange.getText(), parentChange.getChildrens());
            tree.addItem(treeItem);
        }

        tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            @Override
            public void onSelection(SelectionEvent<TreeItem> event) {
                if (selectedElement != null) {
                    selectedElement.getStyle().setProperty("background", "transparent");
                }

                selectedElement = event.getSelectedItem().getWidget().getElement();
                selectedElement.getStyle().setProperty("background", getEditorSelectionColor());
            }
        });

        treePanel.add(tree);
    }

    private void createTreeElement(final TreeItem root, String changeName, List<RefactoringPreview> children) {
        FlowPanel element = new FlowPanel();
        element.getElement().getStyle().setFloat(LEFT);
        CheckBox itemCheckBox = new CheckBox();
        itemCheckBox.setValue(true);
        itemCheckBox.getElement().getStyle().setFloat(LEFT);
        itemCheckBox.getElement().getStyle().setMarginTop(3, PX);
        Label name = new Label(changeName);
        name.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onSelectionChanged(containerChanges.get(root));
                root.setSelected(true);
            }
        });
        name.getElement().getStyle().setFloat(LEFT);
        element.add(itemCheckBox);
        element.add(name);

        root.setWidget(element);

        element.getElement().getParentElement().getStyle().setMargin(1, PX);

        itemCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                checkChildrenState(root, event.getValue());
                checkParentState(root, event.getValue());

                RefactoringPreview change = containerChanges.get(root);
                change.setEnabled(event.getValue());

                delegate.onEnabledStateChanged(change);
            }
        });

        if (children.isEmpty()) {
            return;
        }

        for (RefactoringPreview child : children) {
            TreeItem treeItem = new TreeItem();
            containerChanges.put(treeItem, child);
            createTreeElement(treeItem, child.getText(), child.getChildrens());
            root.addItem(treeItem);
        }
    }

    private void checkParentState(TreeItem treeItem, Boolean value) {
        TreeItem parentItem = treeItem.getParentItem();

        if (parentItem == null) {
            return;
        }

        if (!(parentItem.getWidget() instanceof FlowPanel)) {
            return;
        }
        FlowPanel parentChangeContainer = (FlowPanel)parentItem.getWidget();

        if (!(parentChangeContainer.getWidget(0) instanceof CheckBox)) {
            return;
        }
        CheckBox parentCheckBox = (CheckBox)parentChangeContainer.getWidget(0);

        if (value && !parentCheckBox.getValue()) {
            parentCheckBox.setValue(true);
            checkParentState(parentItem, true);
        }
    }

    private void checkChildrenState(TreeItem treeItem, Boolean value) {
        int childCount = treeItem.getChildCount();
        if (childCount == 0) {
            return;
        }

        for (int i = 0; i < childCount; i++) {
            TreeItem childItem = treeItem.getChild(i);
            if (!(childItem.getWidget() instanceof FlowPanel)) {
                return;
            }
            FlowPanel childItemContainer = (FlowPanel)childItem.getWidget();

            if (!(childItemContainer.getWidget(0) instanceof CheckBox)) {
                return;
            }
            CheckBox childCheckBox = (CheckBox)childItemContainer.getWidget(0);

            childCheckBox.setValue(value);
            checkChildrenState(childItem, value);
        }
    }

    private void showDiffPanel(boolean isVisible) {
        diffPanelToHide.setVisible(isVisible);
        noPreviewToHide.setVisible(!isVisible);
    }

    /** {@inheritDoc} */
    @Override
    public void showErrorMessage(RefactoringStatus status) {
        errorLabel.getElement().getStyle().setColor(Style.getErrorColor());

        showMessage(status);
    }

    /** {@inheritDoc} */
    @Override
    public void showDiff(@Nullable ChangePreview preview) {
        if (preview == null) {
            showDiffPanel(false);
        } else {
            showDiffPanel(true);
            if (compare == null) {
                prepareDiffEditor(preview);
                return;
            }
            refreshComperingFiles(preview);

            compare.reload();
        }
    }

    private void refreshComperingFiles(@NotNull ChangePreview preview) {
        newFile.setContent(preview.getNewContent());
        newFile.setName(preview.getFileName());
        oldFile.setContent(preview.getOldContent());
        oldFile.setName(preview.getFileName());
    }

    private void prepareDiffEditor(@NotNull ChangePreview preview) {
        newFile = compareFactory.createFieOptions();
        newFile.setReadOnly(true);

        oldFile = compareFactory.createFieOptions();
        oldFile.setReadOnly(true);

        refreshComperingFiles(preview);

        CompareConfig compareConfig = compareFactory.createCompareConfig();
        compareConfig.setNewFile(newFile);
        compareConfig.setOldFile(oldFile);
        compareConfig.setShowTitle(true);
        compareConfig.setShowLineStatus(true);

        compare = new CompareWidget(compareConfig, themeAgent.getCurrentThemeId(), loaderFactory);
        diff.add(compare);
    }

    private void showMessage(RefactoringStatus status) {
        RefactoringStatusEntry statusEntry = getEntryMatchingSeverity(status.getSeverity(), status.getEntries());
        if (statusEntry != null) {
            errorLabel.setText(statusEntry.getMessage());
        } else {
            errorLabel.setText("");
        }
    }

    /**
     * Returns the first entry which severity is equal or greater than the
     * given severity. If more than one entry exists that matches the
     * criteria the first one is returned. Returns <code>null</code> if no
     * entry matches.
     *
     * @param severity
     *         the severity to search for. Must be one of <code>FATAL
     *         </code>, <code>ERROR</code>, <code>WARNING</code> or <code>INFO</code>
     * @param entries
     *         list of the refactoring status entries
     * @return the entry that matches the search criteria
     */
    private RefactoringStatusEntry getEntryMatchingSeverity(int severity, List<RefactoringStatusEntry> entries) {
        for (RefactoringStatusEntry entry : entries) {
            if (entry.getSeverity() >= severity)
                return entry;
        }
        return null;
    }

}
