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
package org.eclipse.che.ide.api.editor.texteditor;

import org.eclipse.che.ide.api.editor.text.Position;

public interface CursorModel {

    /**
     * Move cursor to offset.
     *
     * @param offset
     *         the offset
     */
    void setCursorPosition(int offset);


    /**
     * Get cursor position
     *
     * @return the position of cursor.
     */
    Position getCursorPosition();

}
