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
package org.eclipse.che.plugin.debugger.ide.debug.dialogs;

import com.google.inject.assistedinject.Assisted;
import javax.validation.constraints.NotNull;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.common.TextAreaDialogView;

/**
 * Factory for creation text area based dialog.
 *
 * @author Oleksandr Andriienko
 */
public interface DebuggerDialogFactory {

  TextAreaDialogView createTextAreaDialogView(
      @NotNull @Assisted("title") String title,
      @NotNull @Assisted("agreeBtnLabel") String agreeBtnLabel,
      @NotNull @Assisted("cancelBtnLabel") String cancelBtnLabel);
}
