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
package org.eclipse.che.api.project.server.type;

import org.eclipse.che.api.core.model.project.type.Value;

import java.util.Map;

/**
 * @author gazarenkov
 */
public abstract class ProjectTypeResolution {

    private String             type;
    private Map<String, Value> attributes;

    public ProjectTypeResolution(String type, Map<String, Value> attributes) {
        this.type = type;
        this.attributes = attributes;
    }

    /**
     * @return type ID
     */
    public String getType() {
        return type;
    }

    /**
     * @return true if current source code in generally matches project type requirements
     * by default (but not necessarily) it may check if there are all required provided attributes
     */
    public abstract boolean matched();

    /**
     * @return calculated provided attributes
     */
    public Map<String, Value> getProvidedAttributes() {
        return attributes;
    }
}
