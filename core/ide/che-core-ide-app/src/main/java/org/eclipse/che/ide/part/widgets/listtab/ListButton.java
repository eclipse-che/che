/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.part.widgets.listtab;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.PartStackView;

import javax.validation.constraints.NotNull;

/**
 * @author Dmitry Shnurenko
 */
@ImplementedBy(ListButtonWidget.class)
public interface ListButton extends View<ListButton.ActionDelegate> {

    void addListItem(@NotNull ListItem listItem);

    void removeListItem(@NotNull ListItem listItem);

    void setVisible(boolean visible);

    interface ActionDelegate {

        /**
         * Handle clicking on list item
         * @param tab
         */
        void onTabClicked(@NotNull PartStackView.TabItem tab);

        /**
         * Handle clicking on close icon
         * @param tab
         */
        void onTabClose(@NotNull PartStackView.TabItem tab);

    }

}
