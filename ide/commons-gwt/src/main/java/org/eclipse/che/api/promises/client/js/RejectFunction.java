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

import org.eclipse.che.api.promises.client.PromiseError;

public class RejectFunction extends JavaScriptObject {

    protected RejectFunction() {
    }

    public final native void apply(PromiseError error) /*-{
        this(error);
    }-*/;
}
