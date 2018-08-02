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
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.hotkeys.dialog.HotKeysDialogPresenter;

/**
 * Show hotKeys list for IDE and editor
 *
 * @author Alexander Andrienko
 */
public class HotKeysListAction extends AbstractPerspectiveAction {

  private HotKeysDialogPresenter hotKeysDialogPresenter;

  @Inject
  public HotKeysListAction(
      HotKeysDialogPresenter hotKeysDialogPresenter, CoreLocalizationConstant locale) {
    super(null, locale.keyBindingsActionName(), locale.keyBindingsActionDescription());
    this.hotKeysDialogPresenter = hotKeysDialogPresenter;
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {
    hotKeysDialogPresenter.showHotKeys();
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setEnabledAndVisible(true);
  }
}
