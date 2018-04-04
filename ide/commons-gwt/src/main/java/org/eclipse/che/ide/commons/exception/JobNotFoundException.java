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
