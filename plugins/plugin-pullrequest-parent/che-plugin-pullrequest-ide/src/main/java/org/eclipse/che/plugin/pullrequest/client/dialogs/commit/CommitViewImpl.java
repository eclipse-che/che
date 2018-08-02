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
package org.eclipse.che.plugin.pullrequest.client.dialogs.commit;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.plugin.pullrequest.client.ContributeMessages;

/**
 * UI for {@link CommitView}.
 *
 * @author Kevin Pollet
 */
public class CommitViewImpl extends Window implements CommitView {

  /** The UI binder for this component. */
  private static final CommitViewUiBinder UI_BINDER = GWT.create(CommitViewUiBinder.class);

  private final Button ok;

  @UiField(provided = true)
  ContributeMessages messages;

  @UiField TextArea commitDescription;

  @UiField CheckBox includeUntracked;

  private ActionDelegate delegate;

  @Inject
  public CommitViewImpl(final ContributeMessages messages) {
    this.messages = messages;

    setWidget(UI_BINDER.createAndBindUi(this));
    setTitle(messages.commitDialogTitle());

    ok =
        addFooterButton(
            messages.commitDialogButtonOkText(), "commit-dialog-ok", event -> delegate.onOk());

    addFooterButton(
        messages.commitDialogButtonContinueText(),
        "commit-dialog-continue-without-committing",
        event -> delegate.onContinue());

    addFooterButton(
        messages.commitDialogButtonCancelText(),
        "commit-dialog-cancel",
        event -> delegate.onCancel());
  }

  @Override
  public void show(@NotNull String commitDescription) {
    this.commitDescription.setText(commitDescription);
    show(this.commitDescription);
  }

  @Override
  public void close() {
    hide();
  }

  @NotNull
  @Override
  public String getCommitDescription() {
    return commitDescription.getText();
  }

  @Override
  public void setOkButtonEnabled(final boolean enabled) {
    ok.setEnabled(enabled);
  }

  @Override
  public boolean isIncludeUntracked() {
    return includeUntracked.getValue();
  }

  @Override
  public void setDelegate(final ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @SuppressWarnings("UnusedParameters")
  @UiHandler("commitDescription")
  void onCommitDescriptionChanged(final KeyUpEvent event) {
    delegate.onCommitDescriptionChanged();
  }
}
