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
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.util.Flags;
import org.eclipse.che.ide.ext.java.shared.dto.model.ClassFile;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.Field;
import org.eclipse.che.ide.ext.java.shared.dto.model.ImportDeclaration;
import org.eclipse.che.ide.ext.java.shared.dto.model.Initializer;
import org.eclipse.che.ide.ext.java.shared.dto.model.Method;
import org.eclipse.che.ide.ext.java.shared.dto.model.Type;
import org.eclipse.che.ide.ext.java.shared.dto.search.Match;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represent java type.
 * Imports form compilation unit will places as child of this node.
 *
 * @author Evgen Vidolob
 */
public class TypeNode extends AbstractPresentationNode {

    private final JavaResources            resources;
    private final Type                     type;
    private final CompilationUnit          compilationUnit;
    private       NodeFactory              nodeFactory;
    private       ClassFile                classFile;
    private       Map<String, List<Match>> matches;

    @Inject
    public TypeNode(JavaResources resources,
                    NodeFactory nodeFactory,
                    @Assisted Type type,
                    @Nullable @Assisted CompilationUnit compilationUnit,
                    @Assisted ClassFile classFile,
                    @Assisted Map<String, List<Match>> matches) {
        this.resources = resources;
        this.nodeFactory = nodeFactory;
        this.type = type;
        this.compilationUnit = compilationUnit;
        this.classFile = classFile;
        this.matches = matches;
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return PromiseHelper.newPromise(new AsyncPromiseHelper.RequestCall<List<Node>>() {
            @Override
            public void makeCall(AsyncCallback<List<Node>> callback) {
                List<Node> child = new ArrayList<>();
                if (compilationUnit != null && type.isPrimary()) {
                    for (ImportDeclaration importDeclaration : compilationUnit.getImports()) {
                        createNodeForAllMatches(importDeclaration.getHandleIdentifier(), child);
                    }
                    for (Type subType : compilationUnit.getTypes()) {
                        if (subType == type) {
                            continue;
                        }
                        child.add(nodeFactory.create(subType, compilationUnit, classFile, matches));
                    }
                }
                createNodeForAllMatches(type.getHandleIdentifier(), child);

                for (Initializer initializer : type.getInitializers()) {
                    createNodeForAllMatches(initializer.getHandleIdentifier(), child);
                }

                for (Field field : type.getFields()) {
                    createNodeForAllMatches(field.getHandleIdentifier(), child);
                }

                for (Type subType : type.getTypes()) {
                    child.add(nodeFactory.create(subType, compilationUnit, classFile, matches));
                }

                for (Method method : type.getMethods()) {
                    child.add(nodeFactory.create(method, matches, compilationUnit, classFile));
                }

                Collections.sort(child, new NodeComparator());
                callback.onSuccess(child);

            }
        });
    }


    private void createNodeForAllMatches(String id, List<Node> list) {
        if (matches.containsKey(id)) {
            for (Match match : matches.get(id)) {
                list.add(nodeFactory.create(match, compilationUnit, classFile));
            }
        }
    }

    /**
     * Collect all matches for this type node.
     *
     * @return the list of matches.
     */
    public List<Match> getMatches() {
        List<Match> matches = new ArrayList<>();
        if (compilationUnit != null && type.isPrimary()) {
            for (ImportDeclaration importDeclaration : compilationUnit.getImports()) {
                addAllMatches(importDeclaration.getHandleIdentifier(), matches);
            }
        }
        addAllMatches(type.getHandleIdentifier(), matches);

        for (Initializer initializer : type.getInitializers()) {
            addAllMatches(initializer.getHandleIdentifier(), matches);
        }

        for (Field field : type.getFields()) {
            addAllMatches(field.getHandleIdentifier(), matches);
        }

        return matches;
    }

    private void addAllMatches(String id, List<Match> matches) {
        if (this.matches.containsKey(id)) {
            matches.addAll(this.matches.get(id));
        }
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        presentation.setPresentableText(type.getLabel());
        int flags = type.getFlags();
        SVGResource icon;
        if (Flags.isInterface(flags)) {
            icon = resources.interfaceItem();
        } else if (Flags.isEnum(flags)) {
            icon = resources.enumItem();
        } else if (Flags.isAnnotation(flags)) {
            icon = resources.annotationItem();
        } else {
            icon = resources.javaFile();
        }
        presentation.setPresentableIcon(icon);
    }

    @Override
    public String getName() {
        return type.getElementName();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}
