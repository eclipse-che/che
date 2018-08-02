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
package org.eclipse.che.plugin.debugger.ide.debug.tree.node;

import static java.util.Collections.emptyList;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.WatchExpression;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;

/**
 * Watch expression node for display watch Expression information in the debugger tree.
 *
 * @author Oleksandr Andriienko
 */
public class WatchExpressionNode extends AbstractDebuggerNode<WatchExpression> {

  private final PromiseProvider promiseProvider;

  private WatchExpression expression;
  private DebuggerResources debuggerResources;

  @Inject
  public WatchExpressionNode(
      @Assisted WatchExpression expression,
      PromiseProvider promiseProvider,
      DebuggerResources debuggerResources) {
    this.promiseProvider = promiseProvider;
    this.expression = expression;
    this.debuggerResources = debuggerResources;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    // Todo: current server side returns result of evaluation expression like simple string line,
    // so we have no children.
    return promiseProvider.resolve(emptyList());
  }

  @Override
  public String getName() {
    return expression.getExpression();
  }

  @Override
  public boolean isLeaf() {
    // Todo: for current implementation it's an always leaf.
    return true;
  }

  @Override
  public void updatePresentation(NodePresentation presentation) {
    String content = expression.getExpression() + "=" + expression.getResult();
    presentation.setPresentableText(content);
    presentation.setPresentableIcon(debuggerResources.watchExpressionIcon());
  }

  @Override
  public WatchExpression getData() {
    return expression;
  }

  @Override
  public void setData(WatchExpression expression) {
    this.expression = expression;
  }
}
