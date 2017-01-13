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

import org.eclipse.che.ide.api.constraints.Direction;

/**
 * @author Evgen Vidolob
 */
public class EditorMultiPartStackState {

    private EditorPartStack editorPartStack;

    private Direction                 direction;
    private double                    size;
    private EditorMultiPartStackState splitFirst;
    private EditorMultiPartStackState splitSecond;

    public EditorMultiPartStackState(EditorPartStack editorPartStack) {
        this.editorPartStack = editorPartStack;
    }

    public EditorMultiPartStackState(Direction direction,
                                     double size,
                                     EditorMultiPartStackState splitFirst,
                                     EditorMultiPartStackState splitSecond) {
        this.direction = direction;
        this.size = size;
        this.splitFirst = splitFirst;
        this.splitSecond = splitSecond;
    }

    public EditorPartStack getEditorPartStack() {
        return editorPartStack;
    }

    public Direction getDirection() {
        return direction;
    }

    public double getSize() {
        return size;
    }

    public EditorMultiPartStackState getSplitFirst() {
        return splitFirst;
    }

    public EditorMultiPartStackState getSplitSecond() {
        return splitSecond;
    }


}
