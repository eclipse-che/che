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
package org.eclipse.che.plugin.languageserver.ide.rename;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Button;
import javax.inject.Inject;
import org.eclipse.che.ide.ui.UILocalizationConstant;
import org.eclipse.che.ide.ui.dialogs.input.InputDialogFooter;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerLocalization;

/** Rename dialog footer with 'Preview' button */
public class RenameDialogFooter extends InputDialogFooter {

  private final Button previewButton;

  @Inject
  public RenameDialogFooter(
      UILocalizationConstant messages, LanguageServerLocalization localization) {
    super(messages);
    previewButton = new Button();
    previewButton.setText(localization.renameDialogPreviewLabel());
    previewButton.getElement().getStyle().setMarginRight(1, Unit.EM);
    previewButton.addStyleName(resources.windowCss().button());
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
