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
package org.eclipse.che.ide.api.parts;

import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.mvp.Presenter;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Part Stack is tabbed layout element, containing Parts.
 *
 * @author Nikolay Zamosenchuk
 * @author Vitaliy Guliy
 */
public interface PartStack extends Presenter {

    /**
     * State of the part stack.
     */
    enum State {

        /**
         * The default state when the part stack is visible.
         * Part stack can be minimized, maximized or collapsed.
         */
        NORMAL,

        /**
         * Part stack is minimized by minimize button.
         * Having this state part stack can not be maximized or collapsed but
         *  only can be restored by clicking the tab button.
         */
        MINIMIZED,

        /**
         * Part stack is maximized. In this state it can be restored or minimized.
         */
        MAXIMIZED,

        /**
         * Part stack is collapsed while one is maximized.
         * The state will be changed on NORMAL after restoring the perspective.
         */
        COLLAPSED

    }

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
     * Returns the state of the perspective.
     *
     * @return
     *      perspective state
     */
    State getPartStackState();

    /**
     * Maximizes the part stack.
     */
    void maximize();

    /**
     * Collapses the part stack.
     * The part state will be restored when restoring the perspective.
     */
    void collapse();

    /**
     * Minimizes / hides the part stack.
     * The part state will not be retored when restoring the perspective state.
     */
    void minimize();

    /**
     * Restores the part stack and the perspective to the default state.
     */
    void restore();

    /**
     * Displays part menu.
     */
    void showPartMenu(int mouseX, int mouseY);

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

    void setDelegate(ActionDelegate delegate);

    interface ActionDelegate {

        /**
         * Requests the delegate to maximize the part stack.
         *
         * @param partStack
         *          part stack
         */
        void onMaximize(PartStack partStack);

        /**
         * Requests the delegate to  restore part stack state.
         *
         * @param partStack
         *          part stack
         */
        void onRestore(PartStack partStack);

    }

}
