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
package org.eclipse.che.ide.websocket.rest;

import org.eclipse.che.ide.commons.exception.UnmarshallerException;
import org.eclipse.che.ide.websocket.Message;

/**
 * Deserializer for the body of the {@link Message}.
 *
 * <p>By the contract: <code>getPayload()</code> should never return <code>null</code> (should be
 * initialized in impl's constructor and return the same object (with different content) before and
 * after <code>unmarshal()</code>.
 *
 * @param <T>
 * @author Artem Zatsarynnyi
 */
public interface Unmarshallable<T> {
  /**
   * Prepares an object from the incoming {@link Message}.
   *
   * @param response {@link Message}
   */
  void unmarshal(Message response) throws UnmarshallerException;

  /**
   * The content of the returned object normally differs before and after <code>unmarshall()</code>
   * but by the contract it should never be <code>null</code>.
   *
   * @return an object deserialized from the {@link Message}
   */
  T getPayload();
}
