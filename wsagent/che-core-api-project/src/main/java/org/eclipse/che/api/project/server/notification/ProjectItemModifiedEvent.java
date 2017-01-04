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
package org.eclipse.che.api.project.server.notification;

import org.eclipse.che.api.core.notification.EventOrigin;

/**
 * @author gazarenkov
 */
@EventOrigin("project")
public class ProjectItemModifiedEvent {

    public enum EventType {
        UPDATED("updated"),
        CREATED("created"),
        DELETED("deleted"),
        MOVED("moved"),
        RENAMED("renamed");

        private final String value;

        EventType(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private EventType type;
    private String    workspace;
    private String    project;
    private String    path;
    private boolean   folder;
    private String    oldPath;

    public ProjectItemModifiedEvent(EventType type, String workspace, String project, String path, boolean folder) {
        this.type = type;
        this.workspace = workspace;
        this.project = project;
        this.path = path;
        this.folder = folder;
    }

    public ProjectItemModifiedEvent(EventType type, String workspace, String project, String path, boolean folder, String oldPath) {
        this.type = type;
        this.workspace = workspace;
        this.project = project;
        this.path = path;
        this.folder = folder;
        this.oldPath = oldPath;
    }

    public ProjectItemModifiedEvent() {
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isFolder() {
        return folder;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    @Override
    public String toString() {
        return "ProjectItemModifiedEvent{" +
               "type=" + type +
               ", workspace='" + workspace + '\'' +
               ", project='" + project + '\'' +
               ", path='" + path + '\'' +
               ", folder=" + folder +
               ", oldPath='" + oldPath + '\'' +
               '}';
    }
}
