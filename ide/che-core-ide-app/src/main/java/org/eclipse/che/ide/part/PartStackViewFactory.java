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

import com.google.gwt.user.client.ui.FlowPanel;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.parts.PartStackView;

/**
 * Gin factory for PartStackView.
 *
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
public interface PartStackViewFactory {

  /**
   * Creates new instance of {@link PartStackView}. Each call of this method returns new object.
   *
   * @param tabsPanel panel on which tab will be added
   * @return an instance of {@link PartStackView}
   */
  PartStackView create(@NotNull FlowPanel tabsPanel);
}
