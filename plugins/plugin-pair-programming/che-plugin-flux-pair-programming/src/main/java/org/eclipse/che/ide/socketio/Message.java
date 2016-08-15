/*******************************************************************************
 * Copyright (c) 2016 Serli SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Sun Seng David TAN <sunix@sunix.org> - initial API and implementation
 *******************************************************************************/package org.eclipse.che.ide.socketio;

import com.google.gwt.core.client.JavaScriptObject;

public class Message {
    protected String type;
    protected JavaScriptObject json;

    public Message withType(String type){
        this.type = type;
        return this;
    }

    public Message withJsonContent(JavaScriptObject json){
        this.json = json;
        return this;
    }

    public String getType() {
        return type;
    }

    public JavaScriptObject getJsonContent() {
        return json;
    }
}
