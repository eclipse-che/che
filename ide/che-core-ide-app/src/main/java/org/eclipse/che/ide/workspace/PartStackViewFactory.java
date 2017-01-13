/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.workspace;

import com.google.gwt.user.client.ui.FlowPanel;

import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.api.parts.PartStackView.TabPosition;

import javax.validation.constraints.NotNull;

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
     * @param tabPosition
     *         position in which part stack must be located
     * @param tabsPanel
     *         panel on which tab will be added
     * @return an instance of {@link PartStackView}
     */
    PartStackView create(@NotNull TabPosition tabPosition, @NotNull FlowPanel tabsPanel);
}
