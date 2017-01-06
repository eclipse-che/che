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

/**
 * A JS overlay over requirejs modules instances.<br>
 * As this really is any object created by the modules, it's only here to provide semantic : "this is a module loaded by requirejs".
 *
 * @author "Mickaël Leduque"
 */
public class RequirejsModule extends JavaScriptObject {

    protected RequirejsModule() {
    }

}
