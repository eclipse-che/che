/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.model.project.type;

/**
 * Model interface for Project type's attribute
 *
 * @author gazarenkov
 */
public interface Attribute {

  /** @return attribute unique Id */
  String getId();

  /** @return attribute name */
  String getName();

  /** @return project type this attribute belongs to */
  String getProjectType();

  /** @return value for this attribute */
  Value getValue();

  /** @return some test description of this attribute */
  String getDescription();

  /** @return true if the attribute is mandatory */
  boolean isRequired();

  /*
   * @return true if attribute value can be changed
   */
  boolean isVariable();
}
