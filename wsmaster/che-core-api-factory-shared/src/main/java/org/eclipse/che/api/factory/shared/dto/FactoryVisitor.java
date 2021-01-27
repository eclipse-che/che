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

public interface FactoryVisitor {
  FactoryDto visit(FactoryDto factoryDto);

  default FactoryDevfileV2Dto visit(FactoryDevfileV2Dto factoryDto) {
    // most likely nothing to do with Devfile v2 factory as we don't know or touch the structure
    return factoryDto;
  }
}
