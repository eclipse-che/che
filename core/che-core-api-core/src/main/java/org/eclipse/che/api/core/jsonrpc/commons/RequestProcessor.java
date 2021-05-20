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
package org.eclipse.che.api.core.jsonrpc.commons;

/** Platfrom dependent implementation of of request handler processing algorithm. */
public interface RequestProcessor {
  /**
   * Process a runnable interface
   *
   * @param endpointId an endpoint that requested the processing
   * @param runnable runnable to be called for processing of a request
   */
  void process(String endpointId, Runnable runnable);
}
