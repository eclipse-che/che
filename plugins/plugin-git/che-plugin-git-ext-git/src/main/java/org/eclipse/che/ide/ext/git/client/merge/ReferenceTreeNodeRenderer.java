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
package org.eclipse.che.ide.ext.git.client.merge;

import com.google.gwt.resources.client.CssResource;
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.html.SpanElement;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ui.tree.NodeRenderer;
import org.eclipse.che.ide.ui.tree.Tree;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.util.dom.Elements;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * The rendered for reference node.
 *
 * @author Andrey Plotnikov
 */
public class ReferenceTreeNodeRenderer implements NodeRenderer<Reference> {
  public interface Css extends CssResource {
    @ClassName("reference-root")
    String referenceRoot();

    @ClassName("reference-label")
    String referenceLabel();
  }

  public interface Resources extends Tree.Resources {
    @Source("Merge.css")
    Css referenceCss();
  }

  private final Css css;

  private final GitResources gitResources;

  public ReferenceTreeNodeRenderer(Resources res, GitResources gitResources) {
    this.css = res.referenceCss();
    this.css.ensureInjected();
    this.gitResources = gitResources;
  }

  /** {@inheritDoc} */
  @Override
  public Element getNodeKeyTextContainer(SpanElement treeNodeLabel) {
    return (Element) treeNodeLabel.getChildNodes().item(1);
  }

  /** {@inheritDoc} */
  @Override
  public SpanElement renderNodeContents(Reference data) {
    SpanElement root = Elements.createSpanElement(css.referenceRoot());

    SVGImage icon;
    if (data.getFullName().equals(MergePresenter.LOCAL_BRANCHES_TITLE)) {
      icon = new SVGImage(gitResources.checkoutReference());
    } else if (data.getFullName().equals(MergePresenter.REMOTE_BRANCHES_TITLE)) {
      icon = new SVGImage(gitResources.remote());
    } else {
      icon = new SVGImage(gitResources.branches());
    }

    SpanElement label = Elements.createSpanElement(css.referenceLabel());
    String content = data.getDisplayName();
    label.setTextContent(content);

    root.appendChild((Node) icon.getSvgElement().getElement());
    root.appendChild(label);

    return root;
  }

  /** {@inheritDoc} */
  @Override
  public void updateNodeContents(TreeNodeElement<Reference> treeNode) {
    // do nothing
  }
}
