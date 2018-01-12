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
package org.eclipse.che.multiuser.resource.api.license;

import java.util.List;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.resource.model.ProvidedResources;

/**
 * Bridge class that link license and resources granting mechanisms.
 *
 * @author Sergii Leschenko
 */
public interface ResourcesProvider {
  /**
   * Returns list of provided resources for given account.
   *
   * @param accountId account id
   * @return list of provided resources for given account or empty list if there are not any
   *     resources for given account
   * @throws NotFoundException when account with specified id was not found
   * @throws ServerException when some exception occurs
   */
  List<ProvidedResources> getResources(String accountId) throws ServerException, NotFoundException;
}
