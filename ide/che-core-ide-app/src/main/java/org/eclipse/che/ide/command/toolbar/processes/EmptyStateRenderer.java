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
package org.eclipse.che.ide.command.toolbar.processes;

import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.ui.dropdown.DropDownListItem;
import org.eclipse.che.ide.ui.dropdown.DropDownListItemRenderer;

/**
 * Renders widget for representing empty {@link org.eclipse.che.ide.ui.dropdown.DropDownList}.
 */
class EmptyStateRenderer implements DropDownListItemRenderer {

    @Override
    public Widget render(DropDownListItem item) {
        return null;
    }
}
