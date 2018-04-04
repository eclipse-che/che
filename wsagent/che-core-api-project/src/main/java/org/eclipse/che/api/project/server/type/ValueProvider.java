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
