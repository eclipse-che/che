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
package org.eclipse.che.maven.data;

import java.io.Serializable;
import java.util.Objects;

/**
 * Describes maven artifact(project dependency)
 *
 * @author Evgen Vidolob
 */
public class MavenArtifactKey implements Serializable{
    private static final long serialVersionUID = 1L;

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String packaging;
    private final String classifier;

    public MavenArtifactKey(String groupId, String artifactId, String version, String packaging, String classifier) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.packaging = packaging;
        this.classifier = classifier;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getPackaging() {
        return packaging;
    }

    public String getClassifier() {
        return classifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MavenArtifactKey that = (MavenArtifactKey)o;
        return Objects.equals(groupId, that.groupId) &&
               Objects.equals(artifactId, that.artifactId) &&
               Objects.equals(version, that.version) &&
               Objects.equals(packaging, that.packaging) &&
               Objects.equals(classifier, that.classifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version, packaging, classifier);
    }
}
