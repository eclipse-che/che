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
package org.eclipse.che.ide.ext.java.client.refactoring.preview;

import static com.google.gwt.dom.client.Style.Float.LEFT;
import static com.google.gwt.dom.client.Style.Unit.PX;
import static org.eclipse.che.ide.api.theme.Style.getEditorSelectionColor;
import static org.eclipse.che.ide.orion.compare.CompareInitializer.GIT_COMPARE_MODULE;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.orion.compare.CompareConfig;
import org.eclipse.che.ide.orion.compare.CompareFactory;
import org.eclipse.che.ide.orion.compare.CompareInitializer;
import org.eclipse.che.ide.orion.compare.FileOptions;
import org.eclipse.che.ide.orion.compare.jso.GitCompareOverlay;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatus;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatusEntry;
import org.eclipse.che.requirejs.ModuleHolder;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
final class PreviewViewImpl extends Window implements PreviewView {

  interface PreviewViewImplUiBinder extends UiBinder<Widget, PreviewViewImpl> {}

  private static final PreviewViewImplUiBinder UI_BINDER =
      GWT.create(PreviewViewImplUiBinder.class);

  @UiField(provided = true)
  final JavaLocalizationConstant locale;

  @UiField SimplePanel diff;
  @UiField FlowPanel diffPanelToHide;
  @UiField SimplePanel noPreviewToHide;
  @UiField VerticalPanel treePanel;
  @UiField Label errorLabel;

  private ActionDelegate delegate;
  private FileOptions newFile;
  private FileOptions oldFile;
  private GitCompareOverlay compare;

  private final CompareFactory compareFactory;
  private final CompareInitializer compareInitializer;
  private final ModuleHolder moduleHolder;

  private Map<TreeItem, PreviewNode> containerChanges = new HashMap<>();
  private Element selectedElement;

  @Inject
  public PreviewViewImpl(
      JavaLocalizationConstant locale,
      CompareFactory compareFactory,
      CompareInitializer compareInitializer,
      ModuleHolder moduleHolder) {
    this.locale = locale;
    this.compareFactory = compareFactory;
    this.compareInitializer = compareInitializer;
    this.moduleHolder = moduleHolder;

    setTitle(locale.moveDialogTitle());

    setWidget(UI_BINDER.createAndBindUi(this));

    addFooterButton(
        locale.moveDialogButtonBack(),
        "preview-back-button",
        event -> delegate.onBackButtonClicked());

    addFooterButton(
        locale.moveDialogButtonCancel(),
        "preview-cancel-button",
        event -> {
          hide();
          delegate.onCancelButtonClicked();
        });

    addFooterButton(
        locale.moveDialogButtonOk(),
        "preview-ok-button",
        event -> delegate.onAcceptButtonClicked());

    diff.getElement().setId("compareParentDiv");
    showDiff(null);
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    show();
  }

  @Override
  public void close() {
    hide();
  }

  @Override
  protected void onShow() {
    errorLabel.setText("");
    diff.clear();
    compare = null;
  }

  @Override
  public void setTitleCaption(String title) {
    setTitle(title);
  }

  @Override
  public void setTreeOfChanges(Map<String, PreviewNode> nodes) {
    containerChanges.clear();
    showDiffPanel(false);

    final SelectionModel<PreviewNode> selectionModel = new SingleSelectionModel<>();
    selectionModel.addSelectionChangeHandler(
        event -> {
          PreviewNode selectedNode =
              (PreviewNode) ((SingleSelectionModel) selectionModel).getSelectedObject();
          delegate.onSelectionChanged(selectedNode);
        });

    Tree tree = new Tree();

    tree.getElement().setId("tree-of-changes");

    for (PreviewNode parentChange : nodes.values()) {
      TreeItem treeItem = new TreeItem();
      containerChanges.put(treeItem, parentChange);
      createTreeElement(treeItem, parentChange.getDescription(), parentChange.getChildren());
      tree.addItem(treeItem);
    }

    tree.addSelectionHandler(
        event -> {
          if (selectedElement != null) {
            selectedElement.getStyle().setProperty("background", "transparent");
          }

          selectedElement = event.getSelectedItem().getWidget().getElement();
          selectedElement.getStyle().setProperty("background", getEditorSelectionColor());
        });

    treePanel.clear();
    treePanel.add(tree);
  }

  private void createTreeElement(
      final TreeItem root, String changeName, List<PreviewNode> children) {
    FlowPanel element = new FlowPanel();
    element.getElement().getStyle().setFloat(LEFT);
    CheckBox itemCheckBox = new CheckBox();
    itemCheckBox.setValue(true);
    itemCheckBox.getElement().getStyle().setFloat(LEFT);
    itemCheckBox.getElement().getStyle().setMarginTop(3, PX);
    Label name = new Label(changeName);
    name.addClickHandler(
        event -> {
          delegate.onSelectionChanged(containerChanges.get(root));
          root.setSelected(true);
        });
    name.getElement().getStyle().setFloat(LEFT);
    element.add(itemCheckBox);
    element.add(name);

    root.setWidget(element);

    element.getElement().getParentElement().getStyle().setMargin(1, PX);

    itemCheckBox.addValueChangeHandler(
        event -> {
          checkChildrenState(root, event.getValue());
          checkParentState(root, event.getValue());

          PreviewNode change = containerChanges.get(root);
          change.setEnable(event.getValue());

          delegate.onEnabledStateChanged(change);
        });

    if (children.isEmpty()) {
      return;
    }

    for (PreviewNode child : children) {
      TreeItem treeItem = new TreeItem();
      containerChanges.put(treeItem, child);
      createTreeElement(treeItem, child.getDescription(), child.getChildren());
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
    FlowPanel parentChangeContainer = (FlowPanel) parentItem.getWidget();

    if (!(parentChangeContainer.getWidget(0) instanceof CheckBox)) {
      return;
    }
    CheckBox parentCheckBox = (CheckBox) parentChangeContainer.getWidget(0);

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
      FlowPanel childItemContainer = (FlowPanel) childItem.getWidget();

      if (!(childItemContainer.getWidget(0) instanceof CheckBox)) {
        return;
      }
      CheckBox childCheckBox = (CheckBox) childItemContainer.getWidget(0);

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
    }
  }

  private void refreshComperingFiles(@NotNull ChangePreview preview) {
    newFile.setContent(preview.getNewContent());
    oldFile.setContent(preview.getOldContent());

    if (compare != null) {
      compare.update(oldFile, newFile);
    }
  }

  private void prepareDiffEditor(@NotNull ChangePreview preview) {
    newFile = compareFactory.createFieOptions();
    newFile.setReadOnly(true);

    oldFile = compareFactory.createFieOptions();
    oldFile.setReadOnly(true);

    newFile.setName("Refactored Source");
    oldFile.setName("Original Source");

    refreshComperingFiles(preview);

    CompareConfig compareConfig = compareFactory.createCompareConfig();
    compareConfig.setNewFile(oldFile);
    compareConfig.setOldFile(newFile);
    compareConfig.setShowTitle(true);
    compareConfig.setShowLineStatus(true);

    compareInitializer.injectCompareWidget(
        new AsyncCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            JavaScriptObject gitCompare = moduleHolder.getModule(GIT_COMPARE_MODULE);
            compare =
                GitCompareOverlay.create(gitCompare, compareConfig, diff.getElement().getId());
          }

          @Override
          public void onFailure(Throwable caught) {}
        });
  }

  private void showMessage(RefactoringStatus status) {
    RefactoringStatusEntry statusEntry =
        getEntryMatchingSeverity(
            status.getRefactoringSeverity().getValue(), status.getRefactoringStatusEntries());
    if (statusEntry != null) {
      errorLabel.setText(statusEntry.getMessage());
    } else {
      errorLabel.setText("");
    }
  }

  /**
   * Returns the first entry which severity is equal or greater than the given severity. If more
   * than one entry exists that matches the criteria the first one is returned. Returns <code>null
   * </code> if no entry matches.
   *
   * @param severity the severity to search for. Must be one of <code>FATAL
   *         </code>, <code>FAILED</code>, <code>WARNING</code> or <code>INFO</code>
   * @param entries list of the refactoring status entries
   * @return the entry that matches the search criteria
   */
  private RefactoringStatusEntry getEntryMatchingSeverity(
      int severity, List<RefactoringStatusEntry> entries) {
    for (RefactoringStatusEntry entry : entries) {
      if (entry.getRefactoringSeverity().getValue() >= severity) return entry;
    }
    return null;
  }
}
