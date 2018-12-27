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
package org.eclipse.che.plugin.typescript.dto.model;

import java.util.ArrayList;
import java.util.List;

/** Class for storing all Dto TS namespace interfaces */
public class DtoNamespace {

  private final String namespace;
  private List<DtoModel> childs = new ArrayList<>();

  public DtoNamespace(String namespace) {
    this.namespace = namespace;
  }

  /** @return the namespace name */
  public String getNamespace() {
    return namespace;
  }

  /** @return all interfaces that placed in this namespace */
  public List<DtoModel> getDtoInterfaces() {
    return childs;
  }

  /**
   * Add Dto in this namespace
   *
   * @param model the dto
   */
  public void addModel(DtoModel model) {
    childs.add(model);
  }
}
