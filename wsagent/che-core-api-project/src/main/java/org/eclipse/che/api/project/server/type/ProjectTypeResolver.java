/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.type;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.api.core.model.project.type.Value;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.api.ProjectManager;

@Singleton
public class ProjectTypeResolver {

  private final ProjectManager projectManager;

  @Inject
  protected ProjectTypeResolver(
      ProjectManager projectManager) {
    this.projectManager = projectManager;
  }

  public ProjectTypeResolution resolveSources(ProjectType projectType, String projectWsPath) {
    RegisteredProject project = projectManager.getOrNull(projectWsPath);
    if (project == null) {
      return new DefaultResolution(projectType.getId(), newHashMap(), true);
    }

    Map<String, Value> matchAttrs = new HashMap<>();
    for (Attribute attribute : projectType.getAttributes()) {
      String name = attribute.getName();
      if (attribute.isVariable()) {
        Variable var = (Variable) attribute;
        ValueProviderFactory factory = var.getValueProviderFactory();
        if (factory != null) {

          Value value;
          String errorMessage = "";
          try {
            value = new AttributeValue(factory.newInstance(project).getValues(name));
          } catch (ValueStorageException e) {
            value = null;
            errorMessage = e.getLocalizedMessage();
          }

          if (value == null || value.isEmpty()) {
            if (var.isRequired()) {
              // this PT is not match
              errorMessage =
                  errorMessage.isEmpty()
                      ? format("Value for required attribute %s is not initialized", name)
                      : errorMessage;
              return new DefaultResolution(projectType.getId(), errorMessage);
            }
          } else {
            // add one more matched attribute
            matchAttrs.put(name, value);
          }
        }
      }
    }

    return new DefaultResolution(projectType.getId(), matchAttrs, true);
  }

  public static class DefaultResolution extends ProjectTypeResolution {

    private boolean match;

    public DefaultResolution(String type, Map<String, Value> attributes, boolean match) {
      super(type, attributes);
      this.match = match;
    }

    /**
     * Use this one when source code not matches project type requirements
     */
    public DefaultResolution(String type, String resolution) {
      super(type, newHashMap(), resolution);
      this.match = false;
    }

    @Override
    public boolean matched() {
      return match;
    }
  }
}
