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
package org.eclipse.che.mail.template;

import java.util.Map;

/**
 * Holds information that is required for template processing.
 *
 * @author Sergii Leshchenko
 */
public class Template {

  private final String templateName;
  private final Map<String, Object> attributes;

  public Template(String templateName, Map<String, Object> attributes) {
    this.templateName = templateName;
    this.attributes = attributes;
  }

  /**
   * Returns template name.
   *
   * @see ClassLoader#getResource(String)
   */
  public String getName() {
    return templateName;
  }

  /** Returns attributes which will be used while template processing. */
  public Map<String, Object> getAttributes() {
    return attributes;
  }
}
