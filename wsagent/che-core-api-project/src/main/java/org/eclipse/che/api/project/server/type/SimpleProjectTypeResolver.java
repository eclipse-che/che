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

import static java.lang.String.format;
import static java.util.Collections.emptyMap;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.api.core.model.project.type.Value;

@Singleton
public class SimpleProjectTypeResolver implements ProjectTypeResolver {

  @Override
  public ProjectTypeResolution resolve(ProjectType type, String wsPath) {
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
            value = new AttributeValue(factory.newInstance(wsPath).getValues(name));
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
              return new ProjectTypeResolution(type.getId(), emptyMap(), errorMessage) {
                @Override
                public boolean matched() {
                  return false;
                }
              };
            }
          } else {
            // add one more matched attribute
            matchAttrs.put(name, value);
          }
        }
      }
    }

    return new ProjectTypeResolution(type.getId(), matchAttrs) {
      @Override
      public boolean matched() {
        // this is due to inability to properly resolve project type
        // for some configurations (e.g. maven project)
        // must be fixed in future updates
        // return !matchAttrs.isEmpty();
        return true;
      }
    };
  }
}
