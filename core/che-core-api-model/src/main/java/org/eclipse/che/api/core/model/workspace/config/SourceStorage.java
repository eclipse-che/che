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
package org.eclipse.che.api.core.model.workspace.config;

import java.util.Map;

/** @author gazarenkov */
public interface SourceStorage {

  /**
   * The key with this name in the parameters designates the exact revision the source corresponds
   * to. This can be a branch, tag, commit id or anything the particular VCS type understands.
   */
  String REFSPEC_PARAMETER_NAME = "refspec";

  String getType();

  String getLocation();

  Map<String, String> getParameters();
}
