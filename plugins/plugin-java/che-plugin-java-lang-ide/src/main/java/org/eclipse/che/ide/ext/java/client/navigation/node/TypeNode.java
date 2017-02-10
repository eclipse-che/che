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
package org.eclipse.che.ide.ext.java.client.navigation.node;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.api.promises.client.callback.PromiseHelper;
import org.eclipse.che.ide.api.data.tree.HasAction;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.navigation.factory.NodeFactory;
import org.eclipse.che.ide.ext.java.client.navigation.filestructure.FileStructurePresenter;
import org.eclipse.che.ide.ext.java.client.search.node.NodeComparator;
import org.eclipse.che.ide.ext.java.client.util.Flags;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.Field;
import org.eclipse.che.ide.ext.java.shared.dto.model.Initializer;
import org.eclipse.che.ide.ext.java.shared.dto.model.Method;
import org.eclipse.che.ide.ext.java.shared.dto.model.Type;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representation of java type for the java navigation tree.
 *
 * @author Valeriy Svydenko
 */
public class TypeNode extends AbstractPresentationNode implements HasAction {
    private final JavaResources          resources;
    private final NodeFactory            nodeFactory;
    private final FileStructurePresenter fileStructurePresenter;
    private final Type                   type;
    private final CompilationUnit        compilationUnit;
    private final boolean                isShowInheritedMembers;
    private final boolean isFromSuper;

    @Inject
    public TypeNode(JavaResources resources,
                    NodeFactory nodeFactory,
                    FileStructurePresenter fileStructurePresenter,
                    @Assisted Type type,
                    @Assisted CompilationUnit compilationUnit,
                    @Assisted("showInheritedMembers") boolean showInheritedMembers,
                    @Assisted("isFromSuper") boolean isFromSuper) {
        this.resources = resources;
        this.nodeFactory = nodeFactory;
        this.fileStructurePresenter = fileStructurePresenter;
        this.type = type;
        this.compilationUnit = compilationUnit;
        this.isShowInheritedMembers = showInheritedMembers;
        this.isFromSuper = isFromSuper;
    }

    /** {@inheritDoc} */
    @Override
    public Promise<List<Node>> getChildrenImpl() {
        return PromiseHelper.newPromise(new AsyncPromiseHelper.RequestCall<List<Node>>() {
            @Override
            public void makeCall(AsyncCallback<List<Node>> callback) {
                List<Node> child = new ArrayList<>();

                createTypeChildren(child, type, isFromSuper);

                if (type.isPrimary()) {
                    for (Type type : compilationUnit.getSuperTypes()) {
                        createTypeChildren(child, type, true);
                    }
                }

                Collections.sort(child, new NodeComparator());
                callback.onSuccess(child);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        StringBuilder presentableName = new StringBuilder(type.getLabel());
        if (isShowInheritedMembers && !type.isPrimary()) {
            String path = type.getRootPath();
            String className = type.isBinary() ? path.substring(path.lastIndexOf('.') + 1)
                                                 : path.substring(path.lastIndexOf('/') + 1, path.indexOf('.'));

            presentableName.append(" -> ").append(className);
        }

        updatePresentationField(isFromSuper, presentation, presentableName.toString(), resources);

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

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return type.getElementName();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLeaf() {
        return type.getFields().isEmpty() && type.getTypes().isEmpty() && type.getMethods().isEmpty() && type.getInitializers().isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed() {
        fileStructurePresenter.actionPerformed(type);
    }

    private void createTypeChildren(List<Node> child, Type type, boolean isFromSuper) {
        for (Method method : type.getMethods()) {
            if (!method.getLabel().startsWith("<")) {
                child.add(nodeFactory.create(method, isShowInheritedMembers, isFromSuper));
            }
        }

        for (Field field : type.getFields()) {
            child.add(nodeFactory.create(field, isShowInheritedMembers, isFromSuper));
        }

        for (Initializer initializer : type.getInitializers()) {
            child.add(nodeFactory.create(initializer, isShowInheritedMembers, isFromSuper));
        }

        for (Type subType : type.getTypes()) {
            child.add(nodeFactory.create(subType, compilationUnit, isShowInheritedMembers, isFromSuper));
        }
    }

}
