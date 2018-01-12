/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.push;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of {@link PushToRemoteView}.
 *
 * @author Andrey Plotnikov
 * @author Sergii Leschenko
 */
@Singleton
public class PushToRemoteViewImpl extends Window implements PushToRemoteView {
  interface PushToRemoteViewImplUiBinder extends UiBinder<Widget, PushToRemoteViewImpl> {}

  private static PushToRemoteViewImplUiBinder ourUiBinder =
      GWT.create(PushToRemoteViewImplUiBinder.class);

  @UiField ListBox repository;
  @UiField ListBox localBranch;
  @UiField ListBox remoteBranch;
  @UiField CheckBox forcePush;
  Button btnPush;
  Button btnCancel;

  @UiField(provided = true)
  final GitResources res;

  @UiField(provided = true)
  final GitLocalizationConstant locale;

  private ActionDelegate delegate;

  @Inject
  protected PushToRemoteViewImpl(GitResources resources, GitLocalizationConstant locale) {
    this.res = resources;
    this.locale = locale;
    this.ensureDebugId("git-remotes-push-window");

    Widget widget = ourUiBinder.createAndBindUi(this);

    this.setTitle(locale.pushViewTitle());
    this.setWidget(widget);

    btnCancel =
        createButton(
            locale.buttonCancel(),
            "git-remotes-push-cancel",
            new ClickHandler() {

              @Override
              public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
              }
            });
    addButtonToFooter(btnCancel);

    btnPush =
        createButton(
            locale.buttonPush(),
            "git-remotes-push-push",
            new ClickHandler() {

              @Override
              public void onClick(ClickEvent event) {
                delegate.onPushClicked();
              }
            });
    addButtonToFooter(btnPush);
  }

  @Override
  protected void onEnterClicked() {
    if (isWidgetFocused(btnCancel)) {
      delegate.onCancelClicked();
      return;
    }

    if (isWidgetFocused(btnPush)) {
      delegate.onPushClicked();
    }
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public String getRepository() {
    int index = repository.getSelectedIndex();
    return index != -1 ? repository.getItemText(index) : "";
  }

  /** {@inheritDoc} */
  @Override
  public void setRepositories(@NotNull List<Remote> repositories) {
    this.repository.clear();
    for (int i = 0; i < repositories.size(); i++) {
      Remote repository = repositories.get(i);
      this.repository.addItem(repository.getName(), repository.getUrl());
    }
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public String getLocalBranch() {
    int index = localBranch.getSelectedIndex();
    return index != -1 ? localBranch.getItemText(index) : "";
  }

  /** {@inheritDoc} */
  @Override
  public void setLocalBranches(@NotNull List<String> branches) {
    this.localBranch.clear();
    for (int i = 0; i < branches.size(); i++) {
      String branch = branches.get(i);
      this.localBranch.addItem(branch);
    }
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public String getRemoteBranch() {
    int index = remoteBranch.getSelectedIndex();
    return index != -1 ? remoteBranch.getItemText(index) : "";
  }

  /** {@inheritDoc} */
  @Override
  public void setRemoteBranches(@NotNull List<String> branches) {
    this.remoteBranch.clear();
    for (int i = 0; i < branches.size(); i++) {
      String branch = branches.get(i);
      this.remoteBranch.addItem(branch);
    }
  }

  @Override
  public boolean addRemoteBranch(@NotNull String branch) {
    for (int i = 0; i < remoteBranch.getItemCount(); ++i) {
      if (branch.equals(remoteBranch.getItemText(i))) {
        return false;
      }
    }
    remoteBranch.addItem(branch);
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public void setEnablePushButton(final boolean enabled) {
    btnPush.setEnabled(enabled);
    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                btnPush.setFocus(enabled);
              }
            });
  }

  @Override
  public void setSelectedForcePushCheckBox(boolean isSelected) {
    forcePush.setValue(isSelected);
  }

  @Override
  public boolean isForcePushSelected() {
    return forcePush.getValue();
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

  @UiHandler("localBranch")
  public void onLocalBranchValueChanged(ChangeEvent event) {
    delegate.onLocalBranchChanged();
  }

  @UiHandler("repository")
  public void onRepositoryValueChanged(ChangeEvent event) {
    delegate.onRepositoryChanged();
  }

  /** {@inheritDoc} */
  @Override
  public void selectLocalBranch(@NotNull String branch) {
    for (int i = 0; i < localBranch.getItemCount(); i++) {
      if (localBranch.getValue(i).equals(branch)) {
        localBranch.setItemSelected(i, true);
        delegate.onLocalBranchChanged();
        break;
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public void selectRemoteBranch(@NotNull String branch) {
    for (int i = 0; i < remoteBranch.getItemCount(); i++) {
      if (remoteBranch.getValue(i).equals(branch)) {
        remoteBranch.setItemSelected(i, true);
        break;
      }
    }
  }
}
