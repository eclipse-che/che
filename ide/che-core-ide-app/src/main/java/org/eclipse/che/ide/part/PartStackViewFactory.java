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
