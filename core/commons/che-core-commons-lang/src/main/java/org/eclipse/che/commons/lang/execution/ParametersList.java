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
package org.eclipse.che.commons.lang.execution;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Represent and configure program parameters
 */
public class ParametersList {
    private final List<String> parameters = new ArrayList<>();

    public void add(String name, String value) {
        parameters.add(name);
        parameters.add(value);
    }

    /**
     * Adds a parameter without name.
     *
     * @param value
     *         value of the parameter
     */
    public void add(String value) {
        parameters.add(value);
    }

    public List<String> getParameters() {
        return unmodifiableList(parameters);
    }
}
