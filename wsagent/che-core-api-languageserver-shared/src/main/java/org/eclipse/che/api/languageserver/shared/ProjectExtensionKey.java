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
package org.eclipse.che.api.languageserver.shared;

import java.util.Objects;

/**
 * @author Anatoliy Bazko
 */
public class ProjectExtensionKey {
    public static final String ALL_PROJECT_MARKER = "*";

    private String project;
    private String extension;

    private ProjectExtensionKey(String project, String extension) {
        this.project = project;
        this.extension = extension;
    }

    public ProjectExtensionKey() {
    }

    public static ProjectExtensionKey createProjectKey(String project, String extension) {
        return new ProjectExtensionKey(project, extension);
    }

    public static ProjectExtensionKey createAllProjectKey(String extension) {
        return new ProjectExtensionKey(ALL_PROJECT_MARKER, extension);
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProjectExtensionKey))
            return false;
        ProjectExtensionKey that = (ProjectExtensionKey)o;
        return Objects.equals(extension, that.extension) && Objects.equals(project, that.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extension, project);
    }
}
