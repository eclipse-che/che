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
package org.eclipse.che.ide.ext.java.client.editor;

import org.eclipse.che.ide.ext.java.shared.dto.Problem;

/**
 * A callback interface for receiving java problem as they are discovered by some Java operation.
 *
 * @see org.eclipse.che.ide.ext.java.shared.dto.Problem
 */
public interface ProblemRequester {

  /**
   * Notification of a Java problem.
   *
   * @param problem IProblem - The discovered Java problem.
   */
  void acceptProblem(Problem problem);

  /**
   * Notification sent before starting the problem detection process. Typically, this would tell a
   * problem collector to clear previously recorded problems.
   */
  void beginReporting();

  /**
   * Notification sent after having completed problem detection process. Typically, this would tell
   * a problem collector that no more problems should be expected in this iteration.
   */
  void endReporting();

  /**
   * Predicate allowing the problem requestor to signal whether or not it is currently interested by
   * problem reports. When answering <code>false</code>, problem will not be discovered any more
   * until the next iteration.
   *
   * <p>This predicate will be invoked once prior to each problem detection iteration.
   *
   * @return boolean - indicates whether the requestor is currently interested by problems.
   */
  boolean isActive();
}
