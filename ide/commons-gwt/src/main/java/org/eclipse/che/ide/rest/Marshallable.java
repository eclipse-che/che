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

/** Marshaller of a request body to be passed to server */
public interface Marshallable {
  /**
   * @return serialized object Note: the marshaller should have prepared object inside or be the
   *     object itself
   */
  String marshal();
}
