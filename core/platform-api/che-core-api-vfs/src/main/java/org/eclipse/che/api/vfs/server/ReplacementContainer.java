/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.server;

import java.util.HashMap;
import java.util.Map;

/**
 * Container for factory replacement variables sets.
 */
public class ReplacementContainer {
    private Map<String, String> variableProps = new HashMap<>();
    private Map<String, String> textProps     = new HashMap<>();

    public ReplacementContainer() {
    }

    public Map<String, String> getVariableProps() {
        return variableProps;
    }

    public Map<String, String> getTextProps() {
        return textProps;
    }

    public boolean hasReplacements() {
        return getVariableProps().size() > 0 || getTextProps().size() > 0;
    }
}
