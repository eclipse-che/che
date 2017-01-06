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
package org.eclipse.che.ide.part.widgets.partbutton;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.PartStackView.TabItem;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
public interface PartButton extends View<PartButton.ActionDelegate>, TabItem {

    @NotNull
    PartButton setTooltip(@Nullable String tooltip);

    @NotNull
    PartButton setIcon(@Nullable SVGResource resource);

    interface ActionDelegate {
        void onTabClicked(@NotNull TabItem selectedTab);
    }
}
