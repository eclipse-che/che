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
package org.eclipse.che.ide.api.extension;

import java.util.List;

/**
 * Provides Extension information:
 * <ul>
 * <li>id - unique String id;</li>
 * <li>version - version of the Extension;</li>
 * <li>title - brief description of the Extension;</li>
 * <li>dependencies - the list of required dependencies</li>
 * </ul>
 *
 * @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a>
 */
public class ExtensionDescription {
    private final String                      id;
    private final String                      version;
    private final List<DependencyDescription> dependencies;
    private final String                      title;
    private final String                      description;

    /**
     * Construct {@link ExtensionDescription}
     *
     * @param id
     * @param version
     * @param title
     * @param dependencies
     * @param description
     */
    public ExtensionDescription(String id,
                                String version,
                                String title,
                                String description,
                                List<DependencyDescription> dependencies) {
        this.id = id;
        this.version = version;
        this.title = title;
        this.dependencies = dependencies;
        this.description = description;
    }

    /**
     * Get Extension description
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get Extension ID
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Get Extension Version
     *
     * @return
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get Extension title
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the list of {@link DependencyDescription}
     *
     * @return
     */
    public List<DependencyDescription> getDependencies() {
        return dependencies;
    }
}
