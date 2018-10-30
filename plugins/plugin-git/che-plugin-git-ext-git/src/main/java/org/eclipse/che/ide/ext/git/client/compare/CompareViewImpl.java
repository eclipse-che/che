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
package org.eclipse.che.ide.ext.git.client.compare;

import static org.eclipse.che.ide.orion.compare.CompareInitializer.GIT_COMPARE_MODULE;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.orion.compare.CompareConfig;
import org.eclipse.che.ide.orion.compare.CompareFactory;
import org.eclipse.che.ide.orion.compare.CompareInitializer;
import org.eclipse.che.ide.orion.compare.FileOptions;
import org.eclipse.che.ide.orion.compare.jso.GitCompareOverlay;
import org.eclipse.che.ide.ui.button.ButtonAlignment;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.requirejs.ModuleHolder;

/**
 * Implementation of {@link CompareView}.
 *
 * @author Igor Vinokur
 */
@Singleton
final class CompareViewImpl extends Window implements CompareView {

  interface PreviewViewImplUiBinder extends UiBinder<Widget, CompareViewImpl> {}

  private static final PreviewViewImplUiBinder UI_BINDER =
      GWT.create(PreviewViewImplUiBinder.class);

  @UiField DockLayoutPanel dockPanel;
  @UiField SimplePanel comparePanel;
  @UiField Label leftTitle;
  @UiField Label rightTitle;

  @UiField(provided = true)
  final GitLocalizationConstant locale;

  private final Button btnSaveChanges;
  private final Button btnNextDiff;
  private final Button btnPrevDiff;

  private final ModuleHolder moduleHolder;
  private final CompareInitializer compareInitializer;
  private final CompareFactory compareFactory;

  private ActionDelegate delegate;
  private GitCompareOverlay compareWidget;
  private boolean visible;

  @Inject
  public CompareViewImpl(
      CompareFactory compareFactory,
      GitLocalizationConstant locale,
      CompareInitializer compareInitializer,
      ModuleHolder moduleHolder) {
    this.compareFactory = compareFactory;
    this.locale = locale;
    this.compareInitializer = compareInitializer;
    this.moduleHolder = moduleHolder;

    setWidget(UI_BINDER.createAndBindUi(this));

    addFooterButton(locale.buttonClose(), "git-compare-close-btn", event -> hide());
    addFooterButton(
        locale.buttonRefresh(), "git-compare-refresh-btn", event -> compareWidget.refresh());

    btnSaveChanges =
        addFooterButton(
            locale.buttonSaveChanges(),
            "git-compare-save-changes-btn",
            event -> delegate.onSaveChangesClicked(),
            true);
    btnNextDiff =
        addFooterButton(
            locale.buttonNextDiff(),
            "git-compare-next-diff-btn",
            event -> delegate.onNextDiffClicked(),
            ButtonAlignment.LEFT);
    btnPrevDiff =
        addFooterButton(
            locale.buttonPreviousDiff(),
            "git-compare-prev-diff-btn",
            event -> delegate.onPreviousDiffClicked(),
            ButtonAlignment.LEFT);

    comparePanel.getElement().setId(Document.get().createUniqueId());
  }

  @Override
  public void setTitleCaption(String title) {
    setTitle(title);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public String getEditableContent() {
    return compareWidget.getContent();
  }

  @Override
  public void setColumnTitles(String leftTitle, String rightTitle) {
    this.leftTitle.setText(leftTitle);
    this.rightTitle.setText(rightTitle);
  }

  @Override
  public boolean isVisible() {
    return visible;
  }

  @Override
  public void show(String oldContent, String newContent, String fileName, boolean readOnly) {
    dockPanel.setSize(
        String.valueOf((com.google.gwt.user.client.Window.getClientWidth() / 100) * 95) + "px",
        String.valueOf((com.google.gwt.user.client.Window.getClientHeight() / 100) * 90) + "px");

    super.show();
    visible = true;

    FileOptions newFile = compareFactory.createFieOptions();
    newFile.setReadOnly(readOnly);

    FileOptions oldFile = compareFactory.createFieOptions();
    oldFile.setReadOnly(true);

    newFile.setContent(newContent);
    newFile.setName(fileName);
    oldFile.setContent(oldContent);
    oldFile.setName(fileName);

    CompareConfig compareConfig = compareFactory.createCompareConfig();
    compareConfig.setNewFile(newFile);
    compareConfig.setOldFile(oldFile);
    compareConfig.setShowTitle(false);
    compareConfig.setShowLineStatus(false);

    compareInitializer.injectCompareWidget(
        new AsyncCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            JavaScriptObject gitCompare = moduleHolder.getModule(GIT_COMPARE_MODULE);
            compareWidget =
                GitCompareOverlay.create(gitCompare, compareConfig, "gwt-debug-compareParentDiv");
          }

          @Override
          public void onFailure(Throwable caught) {}
        });
  }

  @Override
  public void close() {
    visible = false;
    hide();
  }

  @Override
  public void setEnableSaveChangesButton(boolean enabled) {
    btnSaveChanges.setEnabled(enabled);
  }

  @Override
  public void setEnableNextDiffButton(boolean enabled) {
    btnNextDiff.setEnabled(enabled);
  }

  @Override
  public void setEnablePreviousDiffButton(boolean enabled) {
    btnPrevDiff.setEnabled(enabled);
  }
}
