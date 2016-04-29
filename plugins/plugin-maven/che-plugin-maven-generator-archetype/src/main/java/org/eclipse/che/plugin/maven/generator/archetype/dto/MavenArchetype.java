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
package org.eclipse.che.plugin.maven.generator.archetype.dto;

import org.eclipse.che.dto.shared.DTO;

import java.util.Map;

/**
 * DTO that describes Maven archetype to use for project generation.
 *
 * @author Artem Zatsarynnyi
 */
@DTO
public interface MavenArchetype {

    /** Returns the archetype's groupId. */
    String getGroupId();

    void setGroupId(String groupId);

    MavenArchetype withGroupId(String groupId);

    /** Returns the archetype's artifactId. */
    String getArtifactId();

    void setArtifactId(String artifactId);

    MavenArchetype withArtifactId(String artifactId);

    /** Returns the archetype's version. */
    String getVersion();

    void setVersion(String version);

    MavenArchetype withVersion(String version);

    /** Returns the repository where to find the archetype. */
    String getRepository();

    void setRepository(String repository);

    MavenArchetype withRepository(String repository);

    /** Returns the additional properties for the archetype. */
    Map<String, String> getProperties();

    void setProperties(Map<String, String> properties);

    MavenArchetype withProperties(Map<String, String> properties);
}
