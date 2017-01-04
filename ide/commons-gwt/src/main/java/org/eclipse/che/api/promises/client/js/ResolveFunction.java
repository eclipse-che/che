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
package org.eclipse.che.api.promises.client.js;

import com.google.gwt.core.client.JavaScriptObject;

public class ResolveFunction<V> extends JavaScriptObject {

    protected ResolveFunction() {
    }

    public final native void apply(V arg) /*-{
        this(arg);
    }-*/;
}
