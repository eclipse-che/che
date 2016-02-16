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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.model.workspace.ProjectConfig;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.vfs.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gazarenkov
 */
public class NewProjectConfig implements ProjectConfig {

    private final String path;
    private final String type;
    private final List<String> mixins;
    private final String name;
    private final String description;
    private final Map<String, List<String>> attributes;
    private final SourceStorage origin;

    public NewProjectConfig(String path, String type, List<String> mixins, String name, String description,
                            Map<String, List<String>> attributes, SourceStorage origin) {
        this.path = path;
        this.type = (type == null)? BaseProjectType.ID:type;
        this.mixins = (mixins == null)?new ArrayList<>():mixins;
        this.name = name;
        this.description = description;
        this.attributes = (attributes == null)?new HashMap<>():attributes;
        this.origin = origin;
    }

    public NewProjectConfig(String path, String name, SourceStorage origin) {

        this(path, null, null, name, null, null, origin);
    }

    public NewProjectConfig(Path path) {

        this(path.toString(), null, null, path.getName(), null, null, null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public List<String> getMixins() {
        return mixins;
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return attributes;
    }


    @Override
    public SourceStorage getSource() {
        return origin;
    }
}
