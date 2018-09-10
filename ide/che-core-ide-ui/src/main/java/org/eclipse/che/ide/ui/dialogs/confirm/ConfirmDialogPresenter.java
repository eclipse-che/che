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
package org.eclipse.che.ide.ui.dialogs.confirm;

import static com.google.gwt.safehtml.shared.SimpleHtmlSanitizer.sanitizeHtml;

import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;

/**
 * {@link ConfirmDialog} implementation.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public class ConfirmDialogPresenter implements ConfirmDialog, ConfirmDialogView.ActionDelegate {

  /** This component view. */
  private final ConfirmDialogView view;

  /** The callback used on OK. */
  private final ConfirmCallback confirmCallback;

  /** The callback used on cancel. */
  private final CancelCallback cancelCallback;

  @AssistedInject
  public ConfirmDialogPresenter(
      final @NotNull ConfirmDialogView view,
      final @NotNull @Assisted("title") String title,
      final @NotNull @Assisted("message") String message,
      final @Nullable @Assisted ConfirmCallback confirmCallback,
      final @Nullable @Assisted CancelCallback cancelCallback) {
    this(view, title, new InlineHTML(sanitizeHtml(message)), confirmCallback, cancelCallback);
  }

  @AssistedInject
  public ConfirmDialogPresenter(
      final @NotNull ConfirmDialogView view,
      final @NotNull @Assisted("title") String title,
      final @NotNull @Assisted("message") String message,
      final @Nullable @Assisted("okButtonLabel") String okButtonLabel,
      final @Nullable @Assisted("cancelButtonLabel") String cancelButtonLabel,
      final @Nullable @Assisted ConfirmCallback confirmCallback,
      final @Nullable @Assisted CancelCallback cancelCallback) {
    this(view, title, new InlineHTML(sanitizeHtml(message)), confirmCallback, cancelCallback);

    view.setOkButtonLabel(okButtonLabel);
    view.setCancelButtonLabel(cancelButtonLabel);
  }

  @AssistedInject
  public ConfirmDialogPresenter(
      final @NotNull ConfirmDialogView view,
      final @NotNull @Assisted String title,
      final @NotNull @Assisted IsWidget content,
      final @Nullable @Assisted ConfirmCallback confirmCallback,
      final @Nullable @Assisted CancelCallback cancelCallback) {
    this.view = view;
    this.view.setContent(content);
    this.view.setTitleCaption(title);
    this.confirmCallback = confirmCallback;
    this.cancelCallback = cancelCallback;
    this.view.setDelegate(this);
  }

  @AssistedInject
  public ConfirmDialogPresenter(
      final @NotNull ConfirmDialogView view,
      final @NotNull @Assisted String title,
      final @NotNull @Assisted IsWidget content,
      final @Nullable @Assisted("okButtonLabel") String okButtonLabel,
      final @Nullable @Assisted("cancelButtonLabel") String cancelButtonLabel,
      final @Nullable @Assisted ConfirmCallback confirmCallback,
      final @Nullable @Assisted CancelCallback cancelCallback) {
    this.view = view;
    this.view.setContent(content);
    this.view.setTitleCaption(title);
    this.confirmCallback = confirmCallback;
    this.cancelCallback = cancelCallback;
    this.view.setDelegate(this);

    view.setOkButtonLabel(okButtonLabel);
    view.setCancelButtonLabel(cancelButtonLabel);
  }

  @Override
  public void cancelled() {
    this.view.closeDialog();
    if (this.cancelCallback != null) {
      this.cancelCallback.cancelled();
    }
  }

  @Override
  public void accepted() {
    this.view.closeDialog();
    if (this.confirmCallback != null) {
      this.confirmCallback.accepted();
    }
  }

  @Override
  public void show() {
    this.view.showDialog();
  }

  @Override
  public void onEnterClicked() {
    if (view.isOkButtonInFocus()) {
      accepted();
      return;
    }

    if (view.isCancelButtonInFocus()) {
      cancelled();
    }
  }
}
