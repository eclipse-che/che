/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.search.presentation;

import elemental.html.SpanElement;

import com.google.gwt.dom.client.Element;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.project.shared.SearchOccurrence;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.resources.SearchResult;
import org.eclipse.che.ide.search.factory.FindResultNodeFactory;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.ide.util.dom.Elements;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Instance describe file node where found given text
 *
 * @author Vitalii Parfonov
 */

public class FoundItemNode extends AbstractTreeNode implements HasPresentation {

    private NodePresentation      nodePresentation;
    private FindResultNodeFactory nodeFactory;
    private PromiseProvider       promiseProvider;
    private Resources resources;
    private SearchResult searchResult;
    private String                request;

    @Inject
    public FoundItemNode(FindResultNodeFactory nodeFactory,
                         PromiseProvider promiseProvider,
                         Resources resources,
                         @Assisted SearchResult searchResult,
                         @Assisted String request) {
        this.nodeFactory = nodeFactory;
        this.promiseProvider = promiseProvider;
        this.resources = resources;
        this.searchResult = searchResult;
        this.request = request;
    }

    /** {@inheritDoc} */
    @Override
    public NodePresentation getPresentation(boolean update) {
        if (nodePresentation == null) {
            nodePresentation = new NodePresentation();
            updatePresentation(nodePresentation);
        }

        if (update) {
            updatePresentation(nodePresentation);
        }
        return nodePresentation;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return searchResult.getName();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLeaf() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        StringBuilder resultTitle = new StringBuilder();
        resultTitle.append(" (");
        resultTitle.append(searchResult.getOccurrences().size());
        resultTitle.append(" occurrence");
        if (searchResult.getOccurrences().size() > 1) {
            resultTitle.append('s');
        }
        resultTitle.append(" of '");
        resultTitle.append(request);
        resultTitle.append('\'');
        resultTitle.append(" found)");
        presentation.setPresentableText(resultTitle.toString());
        SpanElement spanElement = Elements.createSpanElement(resources.coreCss().foundItem());
        spanElement.setId(searchResult.getPath());
        spanElement.setInnerText(searchResult.getPath());
        presentation.setUserElement((Element)spanElement);
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        List<Node> fileNodes;
        List<SearchOccurrence> occurrences = searchResult.getOccurrences();
        occurrences.sort(Comparator.comparingInt((SearchOccurrence searchOccurrence) -> searchOccurrence.getLineNumber()));
        fileNodes = occurrences.stream().map(occurrence -> nodeFactory
                .newFoundOccurrenceNode(occurrence, searchResult.getPath())).collect(Collectors.toList());
        return promiseProvider.resolve(fileNodes);
    }
}
