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
package org.eclipse.che.ide.ext.java.client.command.mainclass;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.NodeInterceptor;

/**
 * Interceptor for showing only *.java nodes.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ClassNodeInterceptor implements NodeInterceptor {
  private static final String JAVA_SOURCE = ".java";

  @Override
  public Promise<List<Node>> intercept(Node parent, List<Node> children) {
    List<Node> nodes = new ArrayList<>();

    for (Node child : children) {
      if (!child.isLeaf() || child.getName().endsWith(JAVA_SOURCE)) {
        nodes.add(child);
      }
    }

    return Promises.resolve(nodes);
  }

  @Override
  public int getPriority() {
    return NORM_PRIORITY;
  }
}
