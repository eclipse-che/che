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
