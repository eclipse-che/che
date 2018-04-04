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
