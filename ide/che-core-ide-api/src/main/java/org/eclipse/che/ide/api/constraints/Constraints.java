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
package org.eclipse.che.ide.api.constraints;

/**
 * Represents constraints for some action. Constraints are used to specify
 * action's position in the default group, see {@link org.eclipse.che.ide.api.action.DefaultActionGroup}.
 *
 * @author Evgen Vidolob
 */
public class Constraints {
    public final static Constraints FIRST = new Constraints(Anchor.FIRST, null);
    public final static Constraints LAST  = new Constraints(Anchor.LAST, null);
    /** Anchor. */
    public Anchor    myAnchor;
    public Direction direction;

    /**
     * Id of the action to be positioned relative to. Used when anchor type
     * is either {@link Anchor#AFTER} or {@link Anchor#BEFORE}.
     */
    public String relativeId;

    /**
     * Creates a new constraints instance with the specified anchor type and
     * id of the relative action.
     *
     * @param anchor
     *         anchor
     * @param relativeId
     *         Id of the relative
     */
    public Constraints(Anchor anchor, String relativeId) {
        myAnchor = anchor;
        this.relativeId = relativeId;
    }

    /**
     * Creates a new constraints instance with the specified direction type and
     * id of the relative item.
     *
     * @param direction
     *         direction
     * @param relativeId
     *         Id of the relative
     */
    public Constraints(Direction direction, String relativeId) {
        this.direction = direction;
        this.relativeId = relativeId;
    }

    public Constraints clone() {
        if (myAnchor != null) {
            return new Constraints(myAnchor, relativeId);
        } else if (direction != null) {
            return new Constraints(direction, relativeId);
        }
        return null;
    }
}
