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
package org.eclipse.che.plugin.debugger.ide.debug.tree.node.key;

import static java.lang.String.valueOf;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Objects;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.WatchExpression;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.UniqueKeyProvider;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.VariableNode;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.WatchExpressionNode;

/**
 * Unique key provider for storing debugger node in the {@link Tree}. With help this key provider we
 * can quickly find node in the {@link NodeStorage}.
 *
 * @author Oleksandr Andriienko
 */
@Singleton
public class DebugNodeUniqueKeyProvider implements UniqueKeyProvider<Node> {

  @Inject
  public DebugNodeUniqueKeyProvider() {}

  @Override
  public String getKey(Node item) {
    if (item instanceof VariableNode) {
      Variable variable = ((VariableNode) item).getData();
      return evaluateKey(variable);
    }
    if (item instanceof WatchExpressionNode) {
      WatchExpression expression = ((WatchExpressionNode) item).getData();
      return evaluateKey(expression);
    }
    return evaluateKey(item);
  }

  public String evaluateKey(Variable variable) {
    return Joiner.on("/").join(variable.getVariablePath().getPath());
  }

  public String evaluateKey(WatchExpression expression) {
    return expression.getKey();
  }

  public <T> String evaluateKey(T item) {
    int hash = Objects.hashCode(item);
    return valueOf(hash);
  }
}
