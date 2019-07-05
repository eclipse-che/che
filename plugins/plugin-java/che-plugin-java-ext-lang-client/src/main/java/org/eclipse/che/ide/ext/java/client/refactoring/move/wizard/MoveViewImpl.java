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
package org.eclipse.che.ide.ext.java.client.refactoring.move.wizard;

import static org.eclipse.che.ide.ext.java.client.refactoring.move.MoveType.REFACTOR_MENU;
import static org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType.COMPILATION_UNIT;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.move.MoveType;
import org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType;
import org.eclipse.che.ide.ui.cellview.CellTreeResources;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.jdt.ls.extension.api.dto.JavaProjectStructure;
import org.eclipse.che.jdt.ls.extension.api.dto.PackageFragment;
import org.eclipse.che.jdt.ls.extension.api.dto.PackageFragmentRoot;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatus;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatusEntry;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
final class MoveViewImpl extends Window implements MoveView {
  interface MoveViewImplUiBinder extends UiBinder<Widget, MoveViewImpl> {}

  private static MoveViewImplUiBinder UI_BINDER = GWT.create(MoveViewImplUiBinder.class);

  @UiField SimplePanel icon;
  @UiField TextBox patternField;
  @UiField CheckBox updateFullNames;
  @UiField Label classNameUR;
  @UiField CheckBox updateReferences;
  @UiField ScrollPanel treePanel;
  @UiField Label className;
  @UiField FlowPanel treePanelToHide;
  @UiField FlowPanel patternsPanelToHide;
  @UiField Label errorLabel;

  @UiField(provided = true)
  final JavaLocalizationConstant locale;

  private final CellTreeResources cellTreeResources;
  private final JavaResources resources;

  private ActionDelegate delegate;
  private Button preview;
  private Button accept;

  @Inject
  public MoveViewImpl(
      JavaLocalizationConstant locale,
      CellTreeResources cellTreeResources,
      JavaResources resources) {
    this.locale = locale;
    this.cellTreeResources = cellTreeResources;
    this.resources = resources;

    setTitle(locale.moveDialogTitle());

    setWidget(UI_BINDER.createAndBindUi(this));

    createButtons(locale);

    updateFullNames.addValueChangeHandler(event -> patternField.setEnabled(event.getValue()));
  }

  private void createButtons(JavaLocalizationConstant locale) {
    preview =
        addFooterButton(
            locale.moveDialogButtonPreview(),
            "move-preview-button",
            event -> delegate.onPreviewButtonClicked());

    addFooterButton(
        locale.moveDialogButtonCancel(),
        "move-cancel-button",
        event -> {
          hide();
          delegate.onCancelButtonClicked();
        });

    accept =
        addFooterButton(
            locale.moveDialogButtonOk(),
            "move-accept-button",
            event -> delegate.onAcceptButtonClicked());
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  public void show(RefactorInfo refactorInfo) {
    MoveType moveType = refactorInfo.getMoveType();
    RefactoredItemType refactoredItemType = refactorInfo.getRefactoredItemType();

    treePanelToHide.setVisible(REFACTOR_MENU.equals(moveType));
    patternsPanelToHide.setVisible(COMPILATION_UNIT.equals(refactoredItemType));

    Resource[] selectedItems = refactorInfo.getResources();

    int selectionSize = selectedItems.length;

    boolean isMultiSelection = selectionSize > 1;

    classNameUR.setText(
        isMultiSelection
            ? locale.multiSelectionReferences(selectionSize)
            : selectedItems[0].getName());
    className.setText(
        isMultiSelection
            ? locale.multiSelectionDestination(selectionSize)
            : selectedItems[0].getName());

    show();
  }

  @Override
  public void close() {
    hide();
  }

  /** {@inheritDoc} */
  @Override
  public void clearErrorLabel() {
    errorLabel.setText("");
  }

  /** {@inheritDoc} */
  @Override
  public void setTreeOfDestinations(
      RefactorInfo refactorInfo, List<JavaProjectStructure> projects) {
    final SingleSelectionModel<Object> selectionModel = new SingleSelectionModel<>();
    selectionModel.addSelectionChangeHandler(
        event -> {
          Object object = selectionModel.getSelectedObject();

          if (object instanceof PackageFragmentRoot) {
            PackageFragmentRoot fragmentRoot = (PackageFragmentRoot) object;
            delegate.setMoveDestinationPath(fragmentRoot.getUri(), fragmentRoot.getProjectUri());
          }

          if (object instanceof PackageFragment) {
            PackageFragment fragment = (PackageFragment) object;
            delegate.setMoveDestinationPath(fragment.getUri(), fragment.getProjectUri());
          }
        });
    CellTree tree =
        new CellTree(
            new ProjectsAndPackagesModel(projects, refactorInfo, selectionModel, resources),
            null,
            cellTreeResources);
    tree.setAnimationEnabled(true);
    treePanel.clear();
    treePanel.add(tree);
  }

  /** {@inheritDoc} */
  @Override
  public void showStatusMessage(RefactoringStatus status) {
    errorLabel.getElement().getStyle().setColor(Style.getMainFontColor());

    showMessage(status);
  }

  /** {@inheritDoc} */
  @Override
  public void showErrorMessage(RefactoringStatus status) {
    errorLabel.getElement().getStyle().setColor("#C34d4d");

    showMessage(status);
  }

  /** {@inheritDoc} */
  @Override
  public void setEnablePreviewButton(boolean isEnable) {
    accept.setEnabled(isEnable);
  }

  /** {@inheritDoc} */
  @Override
  public void setEnableAcceptButton(boolean isEnable) {
    preview.setEnabled(isEnable);
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

  /** {@inheritDoc} */
  @Override
  public void clearStatusMessage() {
    errorLabel.setText("");
  }

  /** {@inheritDoc} */
  @Override
  public boolean isUpdateReferences() {
    return updateReferences.getValue();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isUpdateQualifiedNames() {
    return updateFullNames.getValue();
  }

  /** {@inheritDoc} */
  @Override
  public String getFilePatterns() {
    return patternField.getValue();
  }

  /**
   * Returns the first entry which severity is equal or greater than the given severity. If more
   * than one entry exists that matches the criteria the first one is returned. Returns <code>null
   * </code> if no entry matches.
   *
   * @param severity the severity to search for. Must be one of <code>FATAL
   *         </code>, <code>ERROR</code>, <code>WARNING</code> or <code>INFO</code>
   * @param entries list of refactoring status
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
