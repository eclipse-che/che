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
package org.eclipse.che.ide.ui.smartTree.presentation;

import com.google.gwt.dom.client.Element;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ui.smartTree.Tree.Joint;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Base interface for providing node rendering.
 *
 * @author Vlad Zhukovskiy
 */
public interface PresentationRenderer<N extends Node> {

    /**
     * Create root container that has two child: node and descendants containers.
     *
     * @param domID
     *         registered dom id, to allow quick find this node in the dom
     * @return dom element
     */
    Element getRootContainer(String domID);

    /**
     * Create node container that has five child: joint, icon, user element, presentable text and info text containers.
     *
     * @return dom element
     */
    Element getNodeContainer();

    /**
     * Create joint element which calculates by one of three states: open, closed and none.
     *
     * @param joint
     *         joint state
     * @return dom element
     */
    Element getJointContainer(Joint joint);

    /**
     * Create icon container.
     *
     * @param icon
     *         icon that will be displayed
     * @return dom element
     */
    Element getIconContainer(SVGResource icon);

    /**
     * Provide custom user element that may display various information.
     *
     * @param userElement
     *         user element
     * @return dom element
     */
    Element getUserElement(Element userElement);

    /**
     * Create presentable text container, which display main text.
     *
     * @param content
     *         colorized text element
     * @return dom element
     */
    Element getPresentableTextContainer(Element content);

    /**
     * Create info text container, which display additional node information.
     *
     * @param content
     *         colorized text element
     * @return dom element
     */
    Element getInfoTextContainer(Element content);

    /**
     * Create descendants container to display children.
     *
     * @return dom element
     */
    Element getDescendantsContainer();

    /**
     * Render whole node container with descendants.
     *
     * @param node
     *         node to render which
     * @param domID
     *         registered dom id
     * @param joint
     *         joint state
     * @param depth
     *         depth to adjust indent
     * @return rendered node element
     */
    Element render(N node, String domID, Joint joint, int depth);
}
