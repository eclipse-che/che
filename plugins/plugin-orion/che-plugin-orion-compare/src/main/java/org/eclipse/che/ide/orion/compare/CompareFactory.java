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
package org.eclipse.che.ide.orion.compare;

/**
 * Factory for creating Compere widget configuration objects.
 *
 * @author Evgen Vidolob
 */
public interface CompareFactory {

  /**
   * Create new FileOption object.
   *
   * @return new file options.
   */
  FileOptions createFieOptions();

  /**
   * Create new Compare configuration object.
   *
   * @return the compare configuration object.
   */
  CompareConfig createCompareConfig();
}
