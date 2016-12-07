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

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.component.StateComponent;
import org.eclipse.che.ide.api.constraints.Constraints;

import javax.validation.constraints.NotNull;

/**
 * This interface is a general type for all perspectives. You must implement the interface when you add new perspective.
 * Provides methods to define type and view representation of perspective.
 *
 * @author Dmitry Shnurenko
 */
public interface Perspective extends StateComponent {

    /** Maximizes central part stack */
    void maximizeCentralPartStack();

    /** Maximize left part stack */
    void maximizeLeftPartStack();

    /** Maximize right part stack */
    void maximizeRightPartStack();

    /** Maximizes bottom part stack */
    void maximizeBottomPartStack();

    /** Restores perspective to the state before maximizing */
    void restore();

    /** Store perspective state before changing. */
    void storeState();

    /** Restores perspective state after changing. */
    void restoreState();

    /**
     * Sets passed part as active. Sets focus to part and open it.
     *
     * @param part
     *         part which will be active
     * @param type
     *         type of part stack
     */
    void setActivePart(@NotNull PartPresenter part, @NotNull PartStackType type);

    /**
     * Removes part.
     *
     * @param part
     *         part which will be removed
     */
    void removePart(@NotNull PartPresenter part);

    /**
     * Reveals given Part and requests focus for it.
     *
     * @param part
     *         part which wil be focused
     */
    void setActivePart(@NotNull PartPresenter part);

    /**
     * Hides current part.
     *
     * @param part
     *         part which need hide
     */
    void hidePart(@NotNull PartPresenter part);

    /**
     * Opens new Part or shows already opened
     *
     * @param part
     *         part which need open
     * @param type
     *         type of part
     */
    void addPart(@NotNull PartPresenter part, @NotNull PartStackType type);

    /**
     * Opens part with special constraint.
     *
     * @param part
     *         part which will be opened
     * @param type
     *         part stack type to find it among active part stacks
     * @param constraint
     *         constraints with which need open part
     */
    void addPart(@NotNull PartPresenter part, @NotNull PartStackType type, @Nullable Constraints constraint);

    /**
     * Retrieves the instance of the {@link PartStack} for given {@link PartStackType}
     *
     * @param type
     *         type for which need get part stack
     * @return an instance of {@link PartStack}
     */
    @Nullable
    PartStack getPartStack(@NotNull PartStackType type);

    /**
     * Allows perspective to expose it's view to the container.
     *
     * @param container
     *         container in which need expose view
     */
    void go(@NotNull AcceptsOneWidget container);

}
