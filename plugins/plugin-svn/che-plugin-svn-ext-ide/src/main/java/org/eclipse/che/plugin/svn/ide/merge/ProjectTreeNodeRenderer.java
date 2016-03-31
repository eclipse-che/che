/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.svn.ide.merge;

import elemental.dom.Element;
import elemental.html.SpanElement;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.ui.tree.NodeRenderer;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.util.dom.Elements;


/**
 * {@link NodeRenderer} to renderer {@code TreeNode}.
 *
 * TODO temporary moved here, will be removed after resolving IDEX-3045
 *
 * @author Artem Zatsarynnyy
 * @author Vlad Zhukovskyi
 */
public class ProjectTreeNodeRenderer implements NodeRenderer<TreeNode<?>> {

    @Inject
    public ProjectTreeNodeRenderer() {
    }

    @Override
    public Element getNodeKeyTextContainer(SpanElement treeNodeLabel) {
        return (Element)treeNodeLabel.getChildNodes().item(1);
    }

    @Override
    public SpanElement renderNodeContents(TreeNode<?> data) {
        SpanElement root = Elements.createSpanElement();

        root.setInnerText(data.getDisplayName());

        return root;
    }

    @Override
    public void updateNodeContents(TreeNodeElement<TreeNode<?>> treeNode) {
    }
}
