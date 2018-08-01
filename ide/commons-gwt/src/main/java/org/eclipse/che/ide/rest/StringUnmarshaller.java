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
package org.eclipse.che.ide.rest;

import com.google.gwt.http.client.Response;

/**
 * Deserializer for responses body.
 *
 * @author Vitaly Parfonov
 */
public class StringUnmarshaller implements Unmarshallable<String> {
  protected String builder;

  /** {@inheritDoc} */
  @Override
  public void unmarshal(Response response) {
    builder = response.getText();
  }

  /** {@inheritDoc} */
  @Override
  public String getPayload() {
    return builder;
  }
}
