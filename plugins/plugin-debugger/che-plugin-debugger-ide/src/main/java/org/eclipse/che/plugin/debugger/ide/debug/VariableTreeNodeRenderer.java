/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.gwt.resources.client.CssResource;
import elemental.dom.Element;
import elemental.html.SpanElement;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.ide.ui.tree.NodeRenderer;
import org.eclipse.che.ide.ui.tree.Tree;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.util.dom.Elements;

/**
 * Renders variable item on the debugger panel.
 *
 * @see MutableVariable
 * @author Anatolii Bazko
 */
public class VariableTreeNodeRenderer implements NodeRenderer<MutableVariable> {
  public interface Css extends CssResource {
    @ClassName("variable-root")
    String variableRoot();

    @ClassName("variable-label")
    String variableLabel();
  }

  public interface Resources extends Tree.Resources {
    @Source("Debug.css")
    Css variableCss();
  }

  private final Css css;

  public VariableTreeNodeRenderer(@NotNull Resources res) {
    this.css = res.variableCss();
    this.css.ensureInjected();
  }

  @Override
  public Element getNodeKeyTextContainer(@NotNull SpanElement treeNodeLabel) {
    return (Element) treeNodeLabel.getChildNodes().item(1);
  }

  @Override
  public SpanElement renderNodeContents(@NotNull MutableVariable data) {
    SpanElement root = Elements.createSpanElement(css.variableRoot());
    SpanElement label = Elements.createSpanElement(css.variableLabel());
    String content = data.getName() + "=" + data.getValue().getString();
    label.setTextContent(content);

    root.appendChild(label);

    return root;
  }

  @Override
  public void updateNodeContents(@NotNull TreeNodeElement<MutableVariable> treeNode) {
    // do nothing
  }
}
