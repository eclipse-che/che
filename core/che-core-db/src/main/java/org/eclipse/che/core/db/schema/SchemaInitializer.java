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
package org.eclipse.che.core.db.schema;

import java.util.Map;

/**
 * Initializes database schema or migrates an old version of it to a new one.
 *
 * @author Yevhenii Voevodin
 */
public interface SchemaInitializer {

  /**
   * Initializes database schema or migrates an old schema to a new one.
   *
   * @return initialization properties
   * @throws SchemaInitializationException thrown when any error occurs during schema
   *     initialization/migration
   */
  Map<String, String> init() throws SchemaInitializationException;
}
