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
package org.eclipse.che.ide.requirejs;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A store of reference to javascript objects.
 *
 * @author "Mickaël Leduque"
 */
public class ModuleHolder {

    private Map<String, RequirejsModule> modules = new HashMap<>();

    public JavaScriptObject getModule(final String key) {
        return modules.get(key);
    }

    public void setModule(final String key, final RequirejsModule module) {
        this.modules.put(key, module);
    }
}
