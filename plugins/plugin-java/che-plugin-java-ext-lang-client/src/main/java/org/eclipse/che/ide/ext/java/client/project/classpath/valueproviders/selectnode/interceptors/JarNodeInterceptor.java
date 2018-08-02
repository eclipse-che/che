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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.interceptors;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.ext.java.shared.ClasspathEntryKind;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * Interceptor for showing only folder nodes.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class JarNodeInterceptor implements ClasspathNodeInterceptor {
  private static final String JAR = ".jar";

  @Override
  public Promise<List<Node>> intercept(Node parent, List<Node> children) {
    List<Node> nodes = new ArrayList<>();

    for (Node child : children) {
      if (!child.isLeaf() || child.getName().endsWith(JAR)) {
        nodes.add(child);
      }
    }

    return Promises.resolve(nodes);
  }

  @Override
  public int getPriority() {
    return NORM_PRIORITY;
  }

  @Override
  public boolean isNodeValid(Node node) {
    return node.getName().endsWith(JAR);
  }

  @Override
  public int getKind() {
    return ClasspathEntryKind.LIBRARY;
  }
}
