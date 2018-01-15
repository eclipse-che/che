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

/** Marshaller of a request body to be passed to server */
public interface Marshallable {
  /**
   * @return serialized object Note: the marshaller should have prepared object inside or be the
   *     object itself
   */
  String marshal();
}
