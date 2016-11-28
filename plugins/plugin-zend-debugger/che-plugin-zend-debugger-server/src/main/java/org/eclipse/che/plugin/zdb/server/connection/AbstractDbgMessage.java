/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.zdb.server.connection;

/**
 * Abstract Zend debug message (common for client and engine messages).
 *
 * @author Bartlomiej Laczkowski
 */
public abstract class AbstractDbgMessage implements IDbgMessage {

    @Override
    public String getTransferEncoding() {
        return ENCODING;
    }

    @Override
    public void setTransferEncoding(String encoding) {
        // TODO - support user preferred encoding
    }

    @Override
    public String toString() {
        return new StringBuilder(this.getClass().getSimpleName()).append(" [ID=").append(getType()).append(']')
                .toString();
    }

}
