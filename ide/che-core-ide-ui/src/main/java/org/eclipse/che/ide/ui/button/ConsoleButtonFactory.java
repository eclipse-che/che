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
package org.eclipse.che.ide.ui.button;

import javax.validation.constraints.NotNull;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Igor Vinokur */
public interface ConsoleButtonFactory {
  /**
   * Creates console button widget with special icon.
   *
   * @param prompt prompt for current button which is displayed on special popup widget
   * @param resource icon which need set to button
   * @return an instance of {@link ConsoleButton}
   */
  @NotNull
  ConsoleButton createConsoleButton(@NotNull String prompt, @NotNull SVGResource resource);
}
