/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

package org.eclipse.che.ide.js.api.parts;

import javax.validation.constraints.NotNull;
import jsinterop.annotations.JsType;
import org.eclipse.che.ide.api.parts.PartStackType;

/**
 * Handles IDE Perspective, allows to open/close/switch Parts, manages opened Parts.
 *
 * @author Yevhen Vydolob
 */
@JsType
public interface PartManager {

  /**
   * Sets passed part as active. Sets focus to part and open it.
   *
   * @param part part which will be active
   */
  void activatePart(@NotNull Part part);

  /**
   * Check is given part is active
   *
   * @return true if part is active
   */
  boolean isActivePart(Part part);

  /**
   * Opens given Part
   *
   * @param part
   * @param type
   */
  void openPart(Part part, PartStackType type);

  /**
   * Hides given Part
   *
   * @param part
   */
  void hidePart(Part part);

  /**
   * Remove given Part
   *
   * @param part
   */
  void removePart(Part part);
}
