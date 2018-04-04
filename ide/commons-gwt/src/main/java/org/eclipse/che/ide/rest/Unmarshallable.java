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
 * Deserializer for response's body.
 *
 * <p>By the contract: getPayload() should never return null (should be initialized in impl's
 * constructor and return the same object (with different content) before and after unmarshal().
 *
 * @param <T> the return type of the unmarshalled object
 */
public interface Unmarshallable<T> {

  /**
   * Prepares an object from the incoming {@link Response}.
   *
   * @param response incoming response
   */
  void unmarshal(Response response) throws UnmarshallerException;

  /**
   * The content of the returned object normally differs before and after unmarshall() but by the
   * contract it should never be {@code null}.
   *
   * @return the object deserialized from the response
   */
  T getPayload();
}
