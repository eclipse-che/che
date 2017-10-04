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
import static org.eclipse.che.api.project.server.type.ProjectTypeResolver.newDefaultResolution;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.api.core.model.project.type.Value;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.RegisteredProject;

@Singleton
public class SimpleProjectTypeResolver implements ProjectTypeResolver {

  private final ProjectManager projectManager;

  @Inject
  protected SimpleProjectTypeResolver(ProjectManager projectManager) {
    this.projectManager = projectManager;
  }

  @Override
  public ProjectTypeResolution resolve(ProjectType type, String wsPath) {
    RegisteredProject project = projectManager.getOrNull(wsPath);
    if (project == null) {
      return newDefaultResolution(type.getId(), newHashMap(), false);
    }

    Map<String, Value> matchAttrs = new HashMap<>();
    for (Attribute attribute : type.getAttributes()) {
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
              return newDefaultResolution(type.getId(), errorMessage, false);
            }
          } else {
            // add one more matched attribute
            matchAttrs.put(name, value);
          }
        }
      }
    }

    return newDefaultResolution(type.getId(), matchAttrs, true);
  }
}
