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
package org.eclipse.che.api.factory.shared.dto;

/**
 * Visitor that allows us to do necessary updates to the factory, like include default devfile, set
 * project url, set branch of the project etc.
 */
public interface FactoryVisitor {

  /**
   * Visit factory with devfile v1.
   *
   * <p>Implementation should update given factory with needed changes and give it back.
   *
   * @param factoryDto factory to visit
   * @return updated factory
   */
  FactoryDto visit(FactoryDto factoryDto);

  /**
   * Visit factory with devfile v2.
   *
   * <p>Implementation should update given factory and give it back.
   *
   * <p>Che-server does not know devfile v2 structure so most likely we don't want to do anything
   * with it. The default implementation is here for that reason.
   *
   * @param factoryDto factory to visit
   * @return update factory
   */
  default FactoryDevfileV2Dto visit(FactoryDevfileV2Dto factoryDto) {
    // most likely nothing to do with Devfile v2 factory as we don't know or touch the structure
    return factoryDto;
  }
}
