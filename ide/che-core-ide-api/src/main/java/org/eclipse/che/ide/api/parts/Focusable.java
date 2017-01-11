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

/**
 * A view that implements this interface can receive keyboard focus.
 *
 * @author Vitaliy Guliy
 */
public interface Focusable {

    /**
     * Explicitly focus/unfocus this view. Only one view can be focused at a time.
     */
    void setFocus(boolean focused);

    /**
     * Check is this view focused.
     *
     * @return <b>true</b> if view has focus, otherwise return <b>false</b>
     */
    boolean isFocused();

}
