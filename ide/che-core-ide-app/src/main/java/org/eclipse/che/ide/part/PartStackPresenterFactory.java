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
package org.eclipse.che.ide.part;

import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackView;

/**
 * Gin factory for PartStack
 *
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
public interface PartStackPresenterFactory {
  /**
   * Creates new instance of {@link PartStack}. Each call of this method returns new object.
   *
   * @param view special view which is required for creating part stack
   * @param workBenchPartController controller which is used by part stack
   * @return an instance of {@link PartStack}
   */
  PartStack create(
      @NotNull PartStackView view, @NotNull WorkBenchPartController workBenchPartController);
}
