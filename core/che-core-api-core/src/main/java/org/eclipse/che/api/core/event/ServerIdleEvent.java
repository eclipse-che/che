/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.core.event;

public class ServerIdleEvent {
    private long timeout;

    public ServerIdleEvent(long timeout) {
        super();
        this.timeout = timeout;
    }


    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
