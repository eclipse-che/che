/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.zdb.server.variables;

import java.util.List;

import org.eclipse.che.api.debug.shared.model.Variable;

/**
 * Zend debugger specific variable.
 *
 * @author Bartlomiej Laczkowski
 */
public interface IDbgVariable extends Variable {

    @Override
    List<IDbgVariable> getVariables();

    /**
     * Requests child variables computation.
     */
    public void makeComplete();

    /**
     * Assigns new value to this variable.
     *
     * @param newValue
     */
    public void setValue(String newValue);

}
