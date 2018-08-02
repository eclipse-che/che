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
package org.eclipse.che.ide.part;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import javax.validation.constraints.NotNull;

/**
 * Special factory for creating instances of {@link WorkBenchPartController}. Each call of factory
 * method returns new instance.
 *
 * @author Dmitry Shnurenko
 */
public interface WorkBenchControllerFactory {

  /**
   * Creates special controller using throwing parameters.
   *
   * @param parentPanel parent panel
   * @param simplePanel child panel,changes of which should be controlled
   * @return an instance of {@link WorkBenchPartController}
   */
  WorkBenchPartController createController(
      @NotNull SplitLayoutPanel parentPanel, @NotNull SimplePanel simplePanel);
}
