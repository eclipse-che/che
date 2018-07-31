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

/**
 * Job (asynchronous task) not found exception.
 *
 * @author <a href="mailto:azhuleva@exoplatform.com">Ann Shumilova</a>
 * @version $Id: Mar 14, 2012 12:52:17 PM anya $
 */
@SuppressWarnings("serial")
public class JobNotFoundException extends ServerException {

  /** @param response */
  public JobNotFoundException(Response response) {
    super(response);
  }
}
