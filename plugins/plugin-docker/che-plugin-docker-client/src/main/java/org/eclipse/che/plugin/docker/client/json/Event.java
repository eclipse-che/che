/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.client.json;

/**
 * Docker event.
 *
 * @author Alexander Garagatyi
 */
public class Event {
    private String status;
    private String id;
    private String from;
    private long   time;
    private long   timeNano;

    public long getTime() {
        return time;
    }

    public String getFrom() {
        return from;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public long getTimeNano() {
        return timeNano;
    }

    public Event withFrom(String from) {
        this.from = from;
        return this;
    }

    public Event withId(String id) {
        this.id = id;
        return this;
    }

    public Event withStatus(String status) {
        this.status = status;
        return this;
    }

    public Event withTime(long time) {
        this.time = time;
        return this;
    }

    public Event withTimeNano(long timeNano) {
        this.timeNano = timeNano;
        return this;
    }

    @Override
    public String toString() {
        return "Event{" +
               "status='" + status + '\'' +
               ", id='" + id + '\'' +
               ", from='" + from + '\'' +
               ", time=" + time +
               ", timeNano=" + timeNano +
               '}';
    }
}
