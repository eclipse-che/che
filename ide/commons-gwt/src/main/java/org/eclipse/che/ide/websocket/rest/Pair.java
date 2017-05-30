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
package org.eclipse.che.ide.websocket.rest;

import org.eclipse.che.ide.collections.Jso;

/**
 * Pair that may be used to emulate headers of HTTP request/response.
 *
 * @author Artem Zatsarynnyi
 */
public class Pair extends Jso {

    public static Pair create() {
        return Jso.create().cast();
    }

    protected Pair() {
    }

    public final String getName() {
        return getStringField("name");
    }

    public final void setName(String name) {
        addField("name", name);
    }

    public final String getValue() {
        return getStringField("value");
    }

    public final void setValue(String value) {
        addField("value", value);
    }
}