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
package org.eclipse.che.ide.rest;

import com.google.gwt.http.client.Response;
import org.eclipse.che.ide.commons.exception.UnmarshallerException;

/**
 * Unmarshaller for "Location" HTTP Header. Uses in {@link AsyncRequest} for run REST Service
 * asynchronously.
 *
 * @author Evgen Vidolob
 */
public class LocationUnmarshaller implements Unmarshallable<String> {
  private String result;

  /** {@inheritDoc} */
  public void unmarshal(Response response) throws UnmarshallerException {
    result = response.getHeader("Location");
  }

  /** {@inheritDoc} */
  public String getPayload() {
    return result;
  }
}
