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
package org.eclipse.che.ide.ext.git.client.pull;

import static org.eclipse.che.ide.util.dom.DomUtils.isWidgetOrChildFocused;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
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
 * The implementation of {@link PullView}.
 *
 * @author Andrey Plotnikov
 */
@Singleton
public class PullViewImpl extends Window implements PullView {
  interface PullViewImplUiBinder extends UiBinder<Widget, PullViewImpl> {}

  private static PullViewImplUiBinder ourUiBinder = GWT.create(PullViewImplUiBinder.class);

  @UiField ListBox repository;
  @UiField ListBox localBranch;
  @UiField ListBox remoteBranch;
  @UiField CheckBox rebase;
  Button btnPull;
  Button btnCancel;

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
  protected PullViewImpl(GitResources resources, GitLocalizationConstant locale) {
    this.res = resources;
    this.locale = locale;
    this.ensureDebugId("git-remotes-pull-window");

    Widget widget = ourUiBinder.createAndBindUi(this);

    this.setTitle(locale.pullTitle());
    this.setWidget(widget);

    btnCancel =
        addFooterButton(
            locale.buttonCancel(), "git-remotes-pull-cancel", event -> delegate.onCancelClicked());

    btnPull =
        addFooterButton(
            locale.buttonPull(), "git-remotes-pull-pull", event -> delegate.onPullClicked(), true);
  }

  @Override
  public void onEnterPress(NativeEvent evt) {
    if (isWidgetOrChildFocused(btnCancel)) {
      delegate.onCancelClicked();
    } else if (isWidgetOrChildFocused(btnPull)) {
      delegate.onPullClicked();
    }
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public String getRepositoryName() {
    int index = repository.getSelectedIndex();
    return index != -1 ? repository.getItemText(index) : "";
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public String getRepositoryUrl() {
    int index = repository.getSelectedIndex();
    return repository.getValue(index);
  }

  /** {@inheritDoc} */
  @Override
  public void setRepositories(@NotNull List<Remote> repositories) {
    this.repository.clear();
    for (Remote repository : repositories) {
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
    for (String branch : branches) {
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
    for (String branch : branches) {
      this.remoteBranch.addItem(branch);
    }
  }

  @Override
  public boolean getRebase() {
    return rebase.getValue();
  }

  /** {@inheritDoc} */
  @Override
  public void setEnablePullButton(final boolean enabled) {
    btnPull.setEnabled(enabled);
    Scheduler.get().scheduleDeferred(() -> btnPull.setFocus(enabled));
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

  @UiHandler("remoteBranch")
  public void onRemoteBranchValueChanged(ChangeEvent event) {
    delegate.onRemoteBranchChanged();
  }

  @UiHandler("repository")
  public void onRemoteRepositoryValueChanged(ChangeEvent event) {
    delegate.onRemoteRepositoryChanged();
  }

  /** {@inheritDoc} */
  @Override
  public void selectLocalBranch(String branch) {
    for (int i = 0; i < localBranch.getItemCount(); i++) {
      if (localBranch.getValue(i).equals(branch)) {
        localBranch.setItemSelected(i, true);
        break;
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public void selectRemoteBranch(String branch) {
    for (int i = 0; i < remoteBranch.getItemCount(); i++) {
      if (remoteBranch.getValue(i).equals(branch)) {
        remoteBranch.setItemSelected(i, true);
        delegate.onRemoteBranchChanged();
        break;
      }
    }
  }
}
