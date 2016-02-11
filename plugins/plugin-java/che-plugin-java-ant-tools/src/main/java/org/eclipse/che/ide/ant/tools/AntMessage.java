/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ant.tools;

import java.io.Serializable;

/** @author andrew00x */
public class AntMessage implements Serializable {
    public static final int BUILD_ERROR      = -1;
    public static final int BUILD_STARTED    = 1;
    public static final int BUILD_SUCCESSFUL = 1 << 1;
    public static final int BUILD_LOG        = 1 << 2;

    private static final long serialVersionUID = 6112041830147092037L;

    private int    type;
    private String target;
    private String task;
    private String text;

    public AntMessage(int type, String target, String task, String text) {
        this.type = type;
        this.target = target;
        this.task = task;
        this.text = text;
    }

    public AntMessage(int type) {
        this(type, null, null, null);
    }

    public AntMessage() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getText() {
        return text;
    }

    public void setText(String message) {
        this.text = message;
    }

    @Override
    public String toString() {
        return "AntMessage{" +
               "type=" + type +
               ", target='" + target + '\'' +
               ", task='" + task + '\'' +
               ", text='" + text + '\'' +
               '}';
    }
}
