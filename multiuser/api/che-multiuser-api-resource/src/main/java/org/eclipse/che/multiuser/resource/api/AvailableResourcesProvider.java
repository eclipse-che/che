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
package org.eclipse.che.multiuser.resource.api;

import java.util.List;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.resource.model.Resource;

/**
 * Provides resources which are available for usage by account.
 *
 * <p>It can be used for example for implementing resources sharing between accounts or resources
 * usage limitation when limit should be less than resources provided to account.
 *
 * @author Sergii Leschenko
 */
public interface AvailableResourcesProvider {
  /**
   * Returns resources that are available for usage by account with specified id.
   *
   * @param accountId account identifier
   * @return resources that are available for usage by account with specified id.
   * @throws NotFoundException when account with specified id was not found
   * @throws ServerException when some exception occurs
   */
  List<? extends Resource> getAvailableResources(String accountId)
      throws NotFoundException, ServerException;
}
