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
package org.eclipse.che.ide.ui.smartTree.data;

import java.util.List;
import org.eclipse.che.api.promises.client.Promise;

/**
 * Perform children interception to check if current children are available for conversion. This
 * usually useful in case if we want to display instead of file node (*.java) node that represents
 * java class (with inner classes or interfaces) or if file node is under version control system
 * then mark this file with appropriately status in index.
 *
 * @author Vlad Zhukovskiy
 */
public interface NodeInterceptor {

  /** The minimum priority that a interceptor can have. */
  int MIN_PRIORITY = 1;

  /** The default priority that is assigned to a interceptor. */
  int NORM_PRIORITY = 5;

  /** The maximum priority that a interceptor can have. */
  int MAX_PRIORITY = 10;

  /**
   * Intercept nodes and perform operations with them, e.g. settings additional properties, replace
   * ones child with other, etc.
   *
   * @param parent parent node
   * @param children children which should be intercepted and transformed if it need
   * @return intercepted and/or transformed children
   */
  Promise<List<Node>> intercept(Node parent, List<Node> children);

  /**
   * Priority of the run.
   *
   * @return priority for the interceptor in which it should be runned
   */
  int getPriority();
}
