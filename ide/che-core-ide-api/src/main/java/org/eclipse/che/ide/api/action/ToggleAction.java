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
package org.eclipse.che.ide.api.action;

import com.google.gwt.resources.client.ImageResource;

import org.vectomatic.dom.svg.ui.SVGResource;

import org.eclipse.che.commons.annotation.Nullable;

/**
 * An action which has a selected state, and which toggles its selected state when performed.
 * Can be used to represent a menu item with a checkbox, or a toolbar button which keeps its pressed state.
 *
 * @author Evgen Vidolob
 * @author Vitaliy Guliy
 */
public abstract class ToggleAction extends Action implements Toggleable {
    public ToggleAction(@Nullable final String text) {
        super(text);
    }

    public ToggleAction(@Nullable final String text, @Nullable final String description, @Nullable final ImageResource icon,
                        @Nullable final SVGResource svgIcon) {
        super(text, description, icon, svgIcon);

    }

    public ToggleAction(@Nullable final String text, @Nullable final String description, @Nullable final SVGResource svgIcon) {
        super(text, description, null, svgIcon);
    }

    @Override
    public final void actionPerformed(final ActionEvent e) {
        boolean selected = !isSelected(e);
        setSelected(e, selected);

        e.getPresentation().firePropertyChange(SELECTED_PROPERTY, Boolean.valueOf(!selected), Boolean.valueOf(selected));
    }

    /**
     * Returns the selected (checked, pressed) state of the action.
     *
     * @param e
     *         the action event representing the place and context in which the selected state is queried.
     * @return true if the action is selected, false otherwise
     */
    public abstract boolean isSelected(ActionEvent e);

    /**
     * Sets the selected state of the action to the specified value.
     *
     * @param e
     *         the action event which caused the state change.
     * @param state
     *         the new selected state of the action.
     */
    public abstract void setSelected(ActionEvent e, boolean state);

}
