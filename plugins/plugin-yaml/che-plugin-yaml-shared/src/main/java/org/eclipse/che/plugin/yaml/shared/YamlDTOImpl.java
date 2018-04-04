/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
