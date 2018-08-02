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
package org.eclipse.che.plugin.testing.ide.model;

import org.eclipse.che.ide.resource.Path;

/** Locator of the test. */
public interface TestLocator {

  TestLocation getTestLocatio(String locationUrl);

  interface TestLocation {
    /** Returns path of the test class. */
    Path getFilePath();

    /** Returns offset of the test. */
    int getOffset();
  }
}
