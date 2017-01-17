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

import java.util.Collections;
import java.util.List;

/**
 * @author andrew00x
 * @author Anatolii Bazko
 */
public final class JdiNullValue implements JdiValue {
    @Override
    public String getString() {
        return "null";
    }

    @Override
    public List<JdiVariable> getVariables() {
        return Collections.emptyList();
    }

    @Override
    public JdiVariable getVariableByName(String name) {
        return null;
    }
}
