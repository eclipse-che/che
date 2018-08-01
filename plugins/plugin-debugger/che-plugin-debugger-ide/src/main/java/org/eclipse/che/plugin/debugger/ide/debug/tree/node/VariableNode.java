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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

public class VariableNode extends AbstractDebuggerNode<Variable> {

  private final PromiseProvider promiseProvider;
  private Variable data;

  @Inject
  public VariableNode(@Assisted Variable data, PromiseProvider promiseProvider) {
    this.promiseProvider = promiseProvider;
    this.data = data;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return promiseProvider.resolve(children);
  }

  @Override
  public String getName() {
    return data.getName();
  }

  @Override
  public boolean isLeaf() {
    return data.isPrimitive();
  }

  @Override
  public void updatePresentation(NodePresentation presentation) {
    String content = data.getName() + "=" + data.getValue().getString();
    presentation.setPresentableText(content);
  }

  @Override
  public Variable getData() {
    return data;
  }

  @Override
  public void setData(Variable data) {
    this.data = data;
  }
}
