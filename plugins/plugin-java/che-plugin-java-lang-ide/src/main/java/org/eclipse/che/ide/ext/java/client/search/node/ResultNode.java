/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.client.search.node;

import elemental.html.SpanElement;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.api.promises.client.callback.PromiseHelper;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesResponse;
import org.eclipse.che.ide.ext.java.shared.dto.search.Match;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.ide.util.dom.Elements;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tree node represent search result.
 *
 * @author Evgen Vidolob
 */
public class ResultNode extends AbstractPresentationNode {

    private NodeFactory        nodeFactory;
    private FindUsagesResponse response;
    private TreeStyles         styles;
    private JavaResources      resources;

    @Inject
    public ResultNode(TreeStyles styles, JavaResources resources, NodeFactory nodeFactory, @Assisted @NotNull FindUsagesResponse response) {
        this.resources = resources;
        this.nodeFactory = nodeFactory;
        this.response = response;
        this.styles = styles;
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return PromiseHelper.newPromise(new AsyncPromiseHelper.RequestCall<List<Node>>() {
            @Override
            public void makeCall(AsyncCallback<List<Node>> callback) {
                List<Node> projectNodes = new ArrayList<>(response.getProjects().size());
                for (JavaProject javaProject : response.getProjects()) {
                    projectNodes.add(nodeFactory.create(javaProject, response.getMatches()));
                }
                callback.onSuccess(projectNodes);
            }
        });
    }

    @Override
    public String getName() {
        return "Usages of " + response.getSearchElementLabel();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        SpanElement spanElement = Elements.createSpanElement(styles.styles().presentableTextContainer());
        spanElement.setInnerHTML(
                "Usages of <span class=\"" + resources.css().searchMatch() + "\">" + response.getSearchElementLabel() + "</span> [" +
                calculateMatchesSize(response.getMatches()) + " occurrences]");
        presentation.setUserElement((Element)spanElement);

    }

    private String calculateMatchesSize(Map<String, List<Match>> matches) {
        int i = 0;
        for (Map.Entry<String, List<Match>> stringListEntry : matches.entrySet()) {
            i += stringListEntry.getValue().size();
        }

        return String.valueOf(i);
    }
}
