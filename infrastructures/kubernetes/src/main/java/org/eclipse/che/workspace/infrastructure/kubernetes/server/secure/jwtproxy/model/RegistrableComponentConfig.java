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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Map;

/**
 * Describes jwtproxy standard registrable component (type + options map).
 *
 * @author Mykhailo Kuznietsov
 */
@JsonInclude(Include.NON_NULL)
public class RegistrableComponentConfig {
  private String type;
  private Map<String, ?> options;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public RegistrableComponentConfig withType(String type) {
    this.type = type;
    return this;
  }

  public Map<String, ?> getOptions() {
    return options;
  }

  public void setOptions(Map<String, Object> options) {
    this.options = options;
  }

  public RegistrableComponentConfig withOptions(Map<String, ?> options) {
    this.options = options;
    return this;
  }
}
