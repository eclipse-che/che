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
package org.eclipse.che.plugin.testing.ide.detector;

import java.util.Set;

/**
 * Interface for defining extensions for test files. All test framework implementations should
 * implement this interface in order to appear files which can contain tests.
 */
public interface TestFileExtension {
  /*
   * This method returns registered file extensions which can contain tests.
   */
  Set<String> getExtensions();
}
