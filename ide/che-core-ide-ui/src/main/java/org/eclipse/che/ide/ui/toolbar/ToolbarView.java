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
package org.eclipse.che.ide.ui.toolbar;

import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.action.ActionGroup;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link ToolbarPresenter}.
 *
 * @author <a href="mailto:aplotnikov@exoplatform.com">Andrey Plotnikov</a>
 */
public interface ToolbarView extends View<ToolbarView.ActionDelegate> {
  /** Needs for delegate some function into Toolbar view. */
  interface ActionDelegate {}

  void setLeftActionGroup(@NotNull ActionGroup actionGroup);

  void setCenterActionGroup(@NotNull ActionGroup actionGroup);

  void setRightActionGroup(@NotNull ActionGroup actionGroup);

  void setAddSeparatorFirst(boolean addSeparatorFirst);
}
