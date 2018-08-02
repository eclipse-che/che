/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.type;

import java.util.Map;
import org.eclipse.che.api.core.model.project.type.Value;

/** @author gazarenkov */
public abstract class ProjectTypeResolution {

  private String type;
  private Map<String, Value> attributes;
  private String resolution;

  public ProjectTypeResolution(String type, Map<String, Value> attributes) {
    this(type, attributes, "");
  }

  public ProjectTypeResolution(String type, Map<String, Value> attributes, String resolution) {
    this.type = type;
    this.attributes = attributes;
    this.resolution = resolution;
  }

  /** @return type ID */
  public String getType() {
    return type;
  }

  /** @return the reason that current source code NOT matches project type requirements */
  public String getResolution() {
    return resolution;
  }

  /**
   * @return true if current source code in generally matches project type requirements by default
   *     (but not necessarily) it may check if there are all required provided attributes
   */
  public abstract boolean matched();

  /** @return calculated provided attributes */
  public Map<String, Value> getProvidedAttributes() {
    return attributes;
  }
}
