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
package org.eclipse.che.plugin.pullrequest.client.utils;

import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;

/**
 * Helper providing methods to work with factory.
 *
 * @author Kevin Pollet
 */
public final class FactoryHelper {
  private static final String ACCEPT_FACTORY_LINK_REF = "accept";

  /** Disable instantiation. */
  private FactoryHelper() {}

  /**
   * Returns the create project relation link for the given factory.
   *
   * @param factory the factory.
   * @return the create project url or {@code null} if none.
   */
  public static String getAcceptFactoryUrl(@NotNull FactoryDto factory) {
    for (final Link oneLink : factory.getLinks()) {
      if (ACCEPT_FACTORY_LINK_REF.equals(oneLink.getRel())) {
        return oneLink.getHref();
      }
    }
    return null;
  }
}
