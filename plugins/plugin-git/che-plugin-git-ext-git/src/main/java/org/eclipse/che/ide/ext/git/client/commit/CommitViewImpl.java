/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.commit;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelView;
import org.eclipse.che.ide.ui.ShiftableTextArea;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of {@link CommitView}.
 *
 * @author Andrey Plotnikov
 * @author Igor Vinokur
 */
@Singleton
public class CommitViewImpl extends Window implements CommitView {
  interface CommitViewImplUiBinder extends UiBinder<Widget, CommitViewImpl> {}

  private static CommitViewImplUiBinder uiBinder = GWT.create(CommitViewImplUiBinder.class);

  @UiField(provided = true)
  final TextArea message;

  @UiField FlowPanel changesPanel;
  @UiField CheckBox amend;

  @UiField(provided = true)
  final GitResources res;

  @UiField(provided = true)
  final GitLocalizationConstant locale;

  private ListBox remoteBranches;
  private CheckBox pushAfterCommit;
  private Button btnCommit;
  private Button btnCancel;
  private ActionDelegate delegate;

  /** Create view. */
  @Inject
  protected CommitViewImpl(GitResources res, GitLocalizationConstant locale) {
    this.res = res;
    this.locale = locale;
    this.message = new ShiftableTextArea();
    this.ensureDebugId("git-commit-window");

    this.setTitle(locale.commitTitle());

    Widget widget = uiBinder.createAndBindUi(this);
    this.setWidget(widget);

    btnCancel =
        createButton(
            locale.buttonCancel(), "git-commit-cancel", event -> delegate.onCancelClicked());
    btnCommit =
        createButton(
            locale.buttonCommit(), "git-commit-commit", event -> delegate.onCommitClicked());
    btnCommit.addStyleName(resources.windowCss().primaryButton());

    remoteBranches = new ListBox();
    remoteBranches.setEnabled(false);

    pushAfterCommit = new CheckBox();
    pushAfterCommit.setHTML(locale.commitPushCheckboxTitle());
    pushAfterCommit.ensureDebugId("push-after-commit-check-box");
    pushAfterCommit.addValueChangeHandler(event -> remoteBranches.setEnabled(event.getValue()));

    pushAfterCommit.addStyleName(res.gitCSS().spacing());
    getFooter().add(pushAfterCommit);
    getFooter().add(remoteBranches);

    addButtonToFooter(btnCommit);
    addButtonToFooter(btnCancel);
  }

  @Override
  protected void onEnterClicked() {
    if (isWidgetFocused(btnCommit)) {
      delegate.onCommitClicked();
      return;
    }

    if (isWidgetFocused(btnCancel)) {
      delegate.onCancelClicked();
    }
  }

  @NotNull
  @Override
  public String getCommitMessage() {
    return message.getText();
  }

  @Override
  public void setMessage(@NotNull String message) {
    this.message.setText(message);
  }

  @Override
  public void setRemoteBranchesList(List<Branch> branches) {
    remoteBranches.clear();
    branches.forEach(branch -> remoteBranches.addItem(branch.getDisplayName()));
  }

  @Override
  public boolean isAmend() {
    return amend.getValue();
  }

  @Override
  public void setValueToAmendCheckBox(boolean value) {
    amend.setValue(value);
  }

  @Override
  public void setValueToPushAfterCommitCheckBox(boolean value) {
    pushAfterCommit.setValue(value);
  }

  @Override
  public void setEnableAmendCheckBox(boolean enable) {
    amend.setEnabled(enable);
  }

  @Override
  public void setEnablePushAfterCommitCheckBox(boolean enable) {
    pushAfterCommit.setEnabled(enable);
  }

  @Override
  public void setEnableRemoteBranchesDropDownLis(boolean enable) {
    remoteBranches.setEnabled(enable);
  }

  @Override
  public boolean isPushAfterCommit() {
    return pushAfterCommit.getValue();
  }

  @Override
  public void setEnableCommitButton(boolean enable) {
    btnCommit.setEnabled(enable);
  }

  @Override
  public void focusInMessageField() {
    new Timer() {
      @Override
      public void run() {
        message.setFocus(true);
      }
    }.schedule(300);
  }

  @Override
  public void close() {
    this.hide();
  }

  @Override
  public void showDialog() {
    this.show();
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @UiHandler("message")
  public void onMessageChanged(KeyUpEvent event) {
    delegate.onValueChanged();
  }

  @UiHandler("amend")
  public void onAmendValueChange(final ValueChangeEvent<Boolean> event) {
    if (event.getValue()) {
      this.delegate.setAmendCommitMessage();
    } else {
      this.message.setValue("");
    }
    delegate.onValueChanged();
    message.setFocus(true);
  }

  @Override
  public void setChangesPanelView(ChangesPanelView changesPanelView) {
    this.changesPanel.add(changesPanelView);
  }

  @Override
  public String getRemoteBranch() {
    return remoteBranches.getSelectedValue();
  }
}
