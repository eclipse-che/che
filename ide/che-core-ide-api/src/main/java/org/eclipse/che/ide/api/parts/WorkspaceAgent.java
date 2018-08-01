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
package org.eclipse.che.ide.api.parts;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.SDK;

/**
 * Handles IDE Perspective, allows to open/close/switch Parts, manages opened Parts.
 *
 * @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a>
 */
@SDK(title = "ide.api.ui.workspace")
public interface WorkspaceAgent {

  void setActivePart(@NotNull PartPresenter part, PartStackType type);

  /**
   * Opens given Part
   *
   * @param part
   * @param type
   */
  void openPart(PartPresenter part, PartStackType type);

  /**
   * Opens Part with constraint
   *
   * @param part
   * @param type
   * @param constraint
   */
  void openPart(PartPresenter part, PartStackType type, Constraints constraint);

  /**
   * Hides given Part
   *
   * @param part
   */
  void hidePart(PartPresenter part);

  /**
   * Remove given Part
   *
   * @param part
   */
  void removePart(PartPresenter part);

  /**
   * Retrieves the instance of the {@link PartStack} for given {@link PartStackType}
   *
   * @param type one of the enumerated type {@link PartStackType}
   * @return the part stack found, else null
   */
  PartStack getPartStack(PartStackType type);

  /**
   * Get current active part
   *
   * @return the active part
   */
  @Nullable
  PartPresenter getActivePart();

  /**
   * Activate given part
   *
   * @param part
   */
  void setActivePart(PartPresenter part);
}
