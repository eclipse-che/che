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
package org.eclipse.che.plugin.jdb.server.expression;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.VMCannotBeModifiedException;
import com.sun.jdi.Value;

/** @author andrew00x */
public class ArrayElement implements ExpressionValue {
    private final ArrayReference array;
    private final int            indx;
    private       Value          value;

    public ArrayElement(ArrayReference array, int indx) {
        this.array = array;
        this.indx = indx;
    }

    @Override
    public Value getValue() {
        if (value == null) {
            try {
                value = array.getValue(indx);
            } catch (IndexOutOfBoundsException e) {
                throw new ExpressionException(e.getMessage(), e);
            }
        }
        return value;
    }

    @Override
    public void setValue(Value value) {
        try {
            array.setValue(indx, value);
        } catch (InvalidTypeException | ClassNotLoadedException | VMCannotBeModifiedException | IndexOutOfBoundsException e) {
            throw new ExpressionException(e.getMessage(), e);
        }
        this.value = value;
    }
}
