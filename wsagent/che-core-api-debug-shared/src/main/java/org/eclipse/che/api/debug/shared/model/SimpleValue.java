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
package org.eclipse.che.api.debug.shared.model;

import java.util.List;

/**
 * @author Anatoliy Bazko
 */
public interface SimpleValue {
    /**
     * The nested variables.
     */
    List<? extends Variable> getVariables();

    /**
     * The value.
     */
    String getValue();
}
