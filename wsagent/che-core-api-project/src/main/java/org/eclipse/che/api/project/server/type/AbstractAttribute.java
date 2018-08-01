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

import org.eclipse.che.api.core.model.project.type.Attribute;

/** @author gazarenkov */
public abstract class AbstractAttribute implements Attribute {

  protected String projectType;
  protected String name;
  protected String description;
  protected boolean required;
  protected boolean variable;

  protected AbstractAttribute(
      String projectType, String name, String description, boolean required, boolean variable) {
    this.projectType = projectType;
    this.name = name;
    this.description = description;
    this.required = required;
    this.variable = variable;
  }

  public String getId() {
    return projectType + ':' + name;
  }

  public String getProjectType() {
    return projectType;
  }

  public String getDescription() {
    return description;
  }

  public boolean isRequired() {
    return required;
  }

  public boolean isVariable() {
    return variable;
  }

  public String getName() {
    return name;
  }

  public abstract AttributeValue getValue();
}
