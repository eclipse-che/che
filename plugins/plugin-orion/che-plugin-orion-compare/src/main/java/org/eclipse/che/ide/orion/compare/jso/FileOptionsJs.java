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
package org.eclipse.che.ide.orion.compare.jso;

import com.google.gwt.core.client.JavaScriptObject;

import org.eclipse.che.ide.orion.compare.FileOptions;

/**
 * @author Evgen Vidolob
 */
public class FileOptionsJs extends JavaScriptObject implements FileOptions {
    protected FileOptionsJs() {
    }



    @Override
    public final native void setContent(String content) /*-{
        this.Content = content;
    }-*/;

    @Override
    public final native void setName(String name)/*-{
        this.Name = name;
    }-*/;

    @Override
    public final native void setReadOnly(boolean readOnly)/*-{
        this.readonly = readOnly;
    }-*/;
}
