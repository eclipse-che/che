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
 * Defines possible positions of an action relative to another action.
 *
 * @author <a href="mailto:evidolob@codenvy.com">Evgen Vidolob</a>
 * @version $Id:
 */
public enum Anchor {
    /**
     * Anchor type that specifies the action to be the first in the list at the
     * moment of addition.
     */
    FIRST,
    /**
     * Anchor type that specifies the action to be the last in the list at the
     * moment of addition.
     */
    LAST,
    /**
     * Anchor type that specifies the action to be placed before the relative
     * action.
     */
    BEFORE,
    /**
     * Anchor type that specifies the action to be placed after the relative
     * action.
     */
    AFTER
}
