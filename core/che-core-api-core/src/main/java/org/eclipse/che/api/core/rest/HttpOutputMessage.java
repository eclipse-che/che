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
package org.eclipse.che.api.core.rest;

/**
 * HTTP output message.
 *
 * @author andrew00x
 */
public interface HttpOutputMessage extends OutputProvider {
  /** Set HTTP status. */
  void setStatus(int status);

  /**
   * Shortcut to set content-type header. The same may be none with method {@link
   * #setHttpHeader(String, String)}.
   */
  void setContentType(String contentType);

  /**
   * Add HTTP header.
   *
   * @param name name of header
   * @param value value of header
   */
  void addHttpHeader(String name, String value);

  /**
   * Set HTTP header. If the header had already been set, the new value overwrites the previous one.
   *
   * @param name name of header
   * @param value value of header
   */
  void setHttpHeader(String name, String value);
}
