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
package org.eclipse.che.plugin.docker.client.json;

/** @author andrew00x */
public class LxcConfParam {
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "LxcConfParam{" +
               "key='" + key + '\'' +
               ", value='" + value + '\'' +
               '}';
    }

    // -------------------------

    public LxcConfParam withKey(String key) {
        this.key = key;
        return this;
    }

    public LxcConfParam withValue(String value) {
        this.value = value;
        return this;
    }
}
