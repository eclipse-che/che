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
package org.eclipse.che.api.languageserver.remote;

import java.util.Map;
import javax.inject.Singleton;

/** Detects if machine server attributes indicates that we are dealing with language server. */
@Singleton
class LsConfigurationDetector {
  /**
   * Tests attributes for a language server indicator
   *
   * @param attributes map with machine server attributes
   * @return true if language server is detected, false otherwise
   */
  boolean isDetected(Map<String, String> attributes) {
    return "ls".equals(attributes.get("type"));
  }
}
