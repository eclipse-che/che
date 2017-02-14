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
package org.eclipse.che.plugin.docker.client.json;

import com.google.gson.annotations.SerializedName;

/**
 * Docker event.
 *
 * @author Alexander Garagatyi
 * @author Mykola Morhun
 */
public class Event {
    @SerializedName("status")
    private String status;
    @SerializedName("id")
    private String id;
    @SerializedName("from")
    private String from;
    @SerializedName("Type")
    private String type;
    @SerializedName("Action")
    private String action;
    @SerializedName("Actor")
    private Actor  actor;
    @SerializedName("time")
    private long   time;
    @SerializedName("timeNano")
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

    public String getType() {
        return type;
    }

    public String getAction() {
        return action;
    }

    public Actor getActor() {
        return actor;
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

    public Event withType(String type) {
        this.type = type;
        return this;
    }

    public Event withAction(String action) {
        this.action = action;
        return this;
    }

    public Event withActor(Actor actor) {
        this.actor = actor;
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
               ", type='" + type + '\'' +
               ", action='" + action + '\'' +
               ", actor='" + actor + '\'' +
               ", time=" + time +
               ", timeNano=" + timeNano +
               '}';
    }
}
