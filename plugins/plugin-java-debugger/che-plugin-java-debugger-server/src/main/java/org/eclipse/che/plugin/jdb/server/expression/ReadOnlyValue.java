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

import com.sun.jdi.Value;

/** @author andrew00x */
public class ReadOnlyValue implements ExpressionValue {
    private final Value value;

    public ReadOnlyValue(Value value) {
        this.value = value;
    }

    @Override
    public Value getValue() {
        return value;
    }

    @Override
    public void setValue(Value value) {
        throw new ExpressionException("Value is read only. ");
    }
}
