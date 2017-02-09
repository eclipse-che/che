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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.api.promises.client.callback.PromiseHelper;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.util.Flags;
import org.eclipse.che.ide.ext.java.shared.dto.model.ClassFile;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.Method;
import org.eclipse.che.ide.ext.java.shared.dto.search.Match;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents java method
 *
 * @author Evgen Vidolob
 */
public class MethodNode extends AbstractPresentationNode {

    private NodeFactory              nodeFactory;
    private JavaResources            resources;
    private Method                   method;
    private Map<String, List<Match>> matches;
    private CompilationUnit          compilationUnit;
    private ClassFile                classFile;

    @Inject
    public MethodNode(NodeFactory nodeFactory,
                      JavaResources resources,
                      @Assisted Method method,
                      @Assisted Map<String, List<Match>> matches,
                      @Assisted CompilationUnit compilationUnit,
                      @Assisted ClassFile classFile) {
        this.nodeFactory = nodeFactory;
        this.resources = resources;
        this.method = method;
        this.matches = matches;
        this.compilationUnit = compilationUnit;
        this.classFile = classFile;
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return PromiseHelper.newPromise(new AsyncPromiseHelper.RequestCall<List<Node>>() {
            @Override
            public void makeCall(AsyncCallback<List<Node>> callback) {
                List<Node> children = new ArrayList<>();
                if (matches.containsKey(method.getHandleIdentifier())) {
                    for (Match match : matches.get(method.getHandleIdentifier())) {
                        children.add(nodeFactory.create(match, compilationUnit, classFile));
                    }
                }

                Collections.sort(children, new NodeComparator());
                callback.onSuccess(children);
            }
        });
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        //TODO set proper icon
        presentation.setPresentableText(method.getLabel());
        int flags = method.getFlags();
        SVGResource icon;
        if(Flags.isPublic(flags)){
            icon = resources.publicMethod();
        } else if(Flags.isPrivate(flags)){
            icon = resources.privateMethod();
        } else if(Flags.isProtected(flags)){
            icon = resources.protectedMethod();
        } else {
            icon = resources.publicMethod();
        }
        presentation.setPresentableIcon(icon);
    }

    @Override
    public String getName() {
        return method.getElementName();
    }

    @Override
    public boolean isLeaf() {
        return !matches.containsKey(method.getHandleIdentifier());
    }

    public List<Match> getMatches() {
        return matches.get(method.getHandleIdentifier());
    }

}
