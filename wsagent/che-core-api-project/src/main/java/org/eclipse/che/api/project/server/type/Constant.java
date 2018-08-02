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

/** @author gazarenkov */
public final class Constant extends AbstractAttribute {

  private final AttributeValue value;

  public Constant(String projectType, String name, String description, AttributeValue value) {
    super(projectType, name, description, true, false);
    this.value = value;
  }

  public Constant(String projectType, String name, String description, final String str) {
    super(projectType, name, description, true, false);
    this.value = new AttributeValue(str);
  }

  public AttributeValue getValue() {
    return value;
  }
}
