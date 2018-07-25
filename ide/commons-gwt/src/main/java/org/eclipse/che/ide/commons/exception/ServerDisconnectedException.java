/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.commons.exception;

import org.eclipse.che.ide.rest.AsyncRequest;

/** @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a> */
@SuppressWarnings("serial")
public class ServerDisconnectedException extends Exception {

  private AsyncRequest asyncRequest;

  public ServerDisconnectedException(AsyncRequest asyncRequest) {
    this.asyncRequest = asyncRequest;
  }

  public AsyncRequest getAsyncRequest() {
    return asyncRequest;
  }
}
