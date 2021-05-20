/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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
   * The key with this name in the parameters designates the branch that should be initially checked
   * out from the source location.
   */
  String BRANCH_PARAMETER_NAME = "branch";

  /**
   * The key with this name in the parameters designates the tag that the initially checked out
   * branch should be reset to.
   */
  String TAG_PARAMETER_NAME = "tag";

  /**
   * The key with this name in the parameters designates the commit id that the initially checked
   * out branch should be reset to.
   */
  String COMMIT_ID_PARAMETER_NAME = "commitId";

  /**
   * The key with this name in the parameters designates the revision (of any kind) that the
   * initially checked out branch should be reset to.
   */
  String START_POINT_PARAMETER_NAME = "startPoint";

  /**
   * The key with this name in the parameters designates the directory that should be used for
   * sparse checkout, i.e. the only directory of repository which should be created by git.
   */
  String SPARSE_CHECKOUT_DIR_PARAMETER_NAME = "sparseCheckoutDir";

  String getType();

  String getLocation();

  Map<String, String> getParameters();
}
