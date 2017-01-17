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
package org.eclipse.che.plugin.jdb.server.jdi;

import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;

/**
 * Wrapper for {@link com.sun.jdi.Value}
 *
 * @author andrew00x
 * @author Anatolii Bazko
 */
public interface JdiValue extends SimpleValue {

    @Override
    List<JdiVariable> getVariables();

    /**
     * Get nested variable by name.
     *
     * @param name
     *         name of variable. Typically it is name of field. If this value represents array then name should be in form:
     *         <i>[i]</i>, where <i>i</i> is index of element
     * @return nested variable with specified name or <code>null</code> if there is no such variable
     * @see JdiVariable#getName()
     */
    @Nullable
    JdiVariable getVariableByName(String name);
}
