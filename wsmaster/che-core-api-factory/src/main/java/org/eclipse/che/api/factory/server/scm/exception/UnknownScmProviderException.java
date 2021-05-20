/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.factory.server.scm.exception;

/** Thrown when scm provider is unknown */
public class UnknownScmProviderException extends Exception {
  private final String providerUrl;

  public UnknownScmProviderException(String message, String providerUrl) {
    super(message);
    this.providerUrl = providerUrl;
  }

  public String getProviderUrl() {
    return providerUrl;
  }
}
