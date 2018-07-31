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

import com.google.gwt.http.client.Response;
import org.eclipse.che.ide.rest.AsyncRequest;

/**
 * @author Vitaliy Gulyy
 * @author Sergii Leschenko
 */
@SuppressWarnings("serial")
public class UnauthorizedException extends ServerException {

  private AsyncRequest request;

  public UnauthorizedException(Response response, AsyncRequest request) {
    super(response);
    this.request = request;
  }

  public AsyncRequest getRequest() {
    return request;
  }
}
