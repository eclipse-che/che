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
package org.eclipse.che.ide.api.parts;

import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.mvp.Presenter;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Part Stack is tabbed layout element, containing Parts.
 *
 * @author Nikolay Zamosenchuk
 */
public interface PartStack extends Presenter {

    /**
     * Change the focused state of the PartStack to desired value
     *
     * @param focused
     */
    void setFocus(boolean focused);

    /**
     * Add part to the PartStack. To immediately show part, you must call <code>setActivePart()</code>.
     *
     * @param part
     */
    void addPart(PartPresenter part);

    /**
     * Add part to the PartStack with position constraint.
     *
     * @param part
     * @param constraint
     */
    void addPart(PartPresenter part, Constraints constraint);

    /**
     * Ask if PartStack contains given Part.
     *
     * @param part
     * @return
     */
    boolean containsPart(PartPresenter part);

    /**
     * Get active Part. Active is the part that is currently displayed on the screen
     *
     * @return
     */
    PartPresenter getActivePart();

    /**
     * Activate given part (force show it on the screen). If part wasn't previously added
     * to the PartStack or has been removed, that method has no effect.
     *
     * @param part
     */
    void setActivePart(@NotNull PartPresenter part);

    /**
     * Hide given part (remove from the screen). If part not active part that method has no effect.
     *
     * @param part
     */
    void hidePart(PartPresenter part);

    /**
     * Remove given part from PartStack.
     *
     * @param part
     */
    void removePart(PartPresenter part);

    void openPreviousActivePart();

    /**
     * Update part stack reference
     */
    void updateStack();

    /**
     * Get all parts, opened in this stack.
     *
     * @return the parts list
     */
    List<? extends PartPresenter> getParts();
}
