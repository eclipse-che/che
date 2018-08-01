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

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Button;
import javax.inject.Inject;
import org.eclipse.che.ide.ui.UILocalizationConstant;
import org.eclipse.che.ide.ui.dialogs.input.InputDialogFooter;
import org.eclipse.che.ide.ui.window.WindowClientBundle;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerLocalization;

/** Rename dialog footer with 'Preview' button */
public class RenameDialogFooter extends InputDialogFooter {

  private final Button previewButton;

  @Inject
  public RenameDialogFooter(
      UILocalizationConstant messages,
      LanguageServerLocalization localization,
      WindowClientBundle resources) {
    super(messages, resources);
    previewButton = new Button();
    previewButton.setText(localization.renameDialogPreviewLabel());
    previewButton.getElement().getStyle().setMarginRight(1, Unit.EM);
    previewButton.addStyleName(resources.getStyle().windowFrameFooterButton());
    rootPanel.clear();
    rootPanel.add(cancelButton);
    rootPanel.add(previewButton);
    rootPanel.add(okButton);
  }

  void addPreviewClickHandler(Runnable clickHandler) {
    previewButton.addClickHandler(e -> clickHandler.run());
  }

  void setEnabledProceedButtons(boolean enabled) {
    previewButton.setEnabled(enabled);
    okButton.setEnabled(enabled);
  }
}
