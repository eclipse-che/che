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

import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;

/**
 * State of method invocation.
 *
 * @author andrew00x
 */
public interface JdiStackFrame extends StackFrameDump {
    /**
     * Get all available instance or class members.
     *
     * @return list of fields. Fields should be ordered:
     *         <ul>
     *         <li>static fields should go before non-static fields</li>
     *         <li>fields of the same type should be ordered by name</li>
     *         </ul>
     */
    @Override
    List<JdiField> getFields();

    /**
     * Get field by name.
     *
     * @return field or <code>null</code> if there is not such field
     */
    @Nullable
    JdiField getFieldByName(String name);

    @Override
    List<JdiVariable> getVariables();

    /**
     * Get local variable by name.
     *
     * @return local variable or <code>null</code> if there is not such local variable
     */
    @Nullable
    JdiVariable getVariableByName(String name);
}
