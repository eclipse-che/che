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

package org.eclipse.che.api.core.model.project;

import java.util.List;
import java.util.Map;

/**
 * Defines configuration for creating new project
 *
 * @author Roman Nikitenko
 */
public interface NewProjectConfig extends ProjectConfig {
    /** Sets project name */
    void setName(String name);

    /** Sets project path */
    void setPath(String path);

    /** Sets project description */
    void setDescription(String description);

    /** Sets primary project type */
    void setType(String type);

    /** Sets mixin project types */
    void setMixins(List<String> mixins);

    /** Sets project attributes */
    void setAttributes(Map<String, List<String>> attributes);

    /** Sets options for generator to create project */
    void setOptions(Map<String, String> options);

    /** Returns options for generator to create project */
    Map<String, String> getOptions();
}
