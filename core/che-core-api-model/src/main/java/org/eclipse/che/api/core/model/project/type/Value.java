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
package org.eclipse.che.api.core.model.project.type;

import java.util.List;

/**
 * Attribute value
 * @author gazarenkov
 */
public interface Value {

    /**
     * @return value as String. If attribute has multiple values it returns first one.
     */
    String getString();

    /**
     * @return value as list of strings
     */
    List<String> getList();

    /**
     * @return whether the value is not initialized
     */
    boolean isEmpty();

}
