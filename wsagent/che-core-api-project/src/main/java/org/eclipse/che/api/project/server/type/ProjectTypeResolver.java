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

import static java.util.Collections.emptyMap;

import java.util.Map;
import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.api.core.model.project.type.Value;

public interface ProjectTypeResolver {

  static ProjectTypeResolution newDefaultResolution(
      String type, Map<String, Value> attributes, boolean match) {
    return new ProjectTypeResolution(type, attributes) {
      @Override
      public boolean matched() {
        return match;
      }
    };
  }

  static ProjectTypeResolution newDefaultResolution(String type, String resolution, boolean match) {
    return new ProjectTypeResolution(type, emptyMap(), resolution) {
      @Override
      public boolean matched() {
        return match;
      }
    };
  }

  ProjectTypeResolution resolve(ProjectType type, String wsPath);
}
