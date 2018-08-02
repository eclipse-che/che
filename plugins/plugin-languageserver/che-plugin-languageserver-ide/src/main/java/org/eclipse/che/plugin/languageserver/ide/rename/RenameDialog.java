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
package org.eclipse.che.plugin.languageserver.ide.rename;

import java.util.function.Consumer;
import javax.inject.Inject;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.input.InputDialogViewImpl;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerLocalization;

/** Rename dialog form, extends {@link InputDialogViewImpl} has additional 'Preview' button */
public class RenameDialog extends InputDialogViewImpl {

  private final RenameDialogFooter dialogFooter;
  private final LanguageServerLocalization localization;

  @Inject
  public RenameDialog(RenameDialogFooter dialogFooter, LanguageServerLocalization localization) {
    super(dialogFooter);
    this.dialogFooter = dialogFooter;
    this.localization = localization;
  }

  public void show(
      String value,
      String oldName,
      Consumer<String> rename,
      Consumer<String> preview,
      CancelCallback cancelCallback) {
    setTitleCaption(localization.renameViewTitle());
    setContent(localization.renameDialogLabel());
    setValue(value);
    setSelectionStartIndex(0);
    setSelectionLength(value.length());
    dialogFooter.addPreviewClickHandler(() -> preview.accept(getValue()));
    dialogFooter.setEnabledProceedButtons(false);
    setDelegate(
        new ActionDelegate() {
          @Override
          public void cancelled() {
            cancelCallback.cancelled();
          }

          @Override
          public void accepted() {
            rename.accept(getValue());
          }

          @Override
          public void inputValueChanged() {
            if (getValue().equals(oldName)) {
              dialogFooter.setEnabledProceedButtons(false);
            } else {
              dialogFooter.setEnabledProceedButtons(true);
            }
          }

          @Override
          public void onEnterClicked() {
            rename.accept(getValue());
          }
        });
    showDialog();
  }
}
