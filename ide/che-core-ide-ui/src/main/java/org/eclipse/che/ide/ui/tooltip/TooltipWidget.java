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
package org.eclipse.che.ide.ui.tooltip;

import com.google.inject.ImplementedBy;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Provides methods which allow work with tooltip widget.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(TooltipWidgetImpl.class)
public interface TooltipWidget {

  /**
   * Sets description which will be displayed on tooltip.
   *
   * @param description description which need set
   */
  void setDescription(@NotNull String description);

  /**
   * Sets coordinates where will be displayed tooltip.
   *
   * @param x value of x coordinate
   * @param y value of y coordinate
   */
  void setPopupPosition(@Min(value = 0) int x, @Min(value = 0) int y);

  /** Shows tooltip. */
  void show();

  /** Hides tooltip. */
  void hide();
}
