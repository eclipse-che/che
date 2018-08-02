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
package org.eclipse.che.api.project.server.type;

import java.util.List;

/**
 * Provides access to the value of attribute of Project.
 *
 * @author andrew00x
 * @author gazarenkov
 */
public interface ValueProvider {

  /** Gets values. */
  List<String> getValues(String attributeName) throws ValueStorageException;

  /**
   * Sets values
   *
   * @param attributeName - name
   * @param values - values
   * @throws ValueStorageException
   */
  void setValues(String attributeName, List<String> values) throws ValueStorageException;

  /** @return whether this Value Provider intended to initialize values */
  boolean isSettable();
}
