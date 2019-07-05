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
package org.eclipse.che.plugin.yaml.shared;

import java.util.Map;

/**
 * Implementation of {@link YamlDTO}
 *
 * @author Joshua Pinkney
 */
public class YamlDTOImpl implements YamlDTO {

  private Map<String, String> schemas;

  /** {@inheritDoc} */
  @Override
  public Map<String, String> getSchemas() {
    return this.schemas;
  }

  /** {@inheritDoc} */
  @Override
  public void setSchemas(Map<String, String> schemas) {
    this.schemas = schemas;
  }

  @Override
  public YamlDTO withSchemas(Map<String, String> schemas) {
    this.schemas = schemas;
    return this;
  }
}
