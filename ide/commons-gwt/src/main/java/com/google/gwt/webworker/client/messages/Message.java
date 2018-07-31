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
package com.google.gwt.webworker.client.messages;

/**
 * Base interface for all DTOs that adds a type tag for routing messages.
 *
 * @author <a href="mailto:evidolob@codenvy.com">Evgen Vidolob</a>
 */
public interface Message {
  int NON_ROUTABLE_TYPE = -2;
  String TYPE_FIELD = "_type";

  /** Every DTO needs to report a type for the purposes of routing messages on the client. */
  int getType();
}
