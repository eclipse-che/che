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
import org.eclipse.che.ide.ext.java.shared.dto.model.ClassFile;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.PackageFragment;
import org.eclipse.che.ide.ext.java.shared.dto.model.PackageFragmentRoot;
import org.eclipse.che.ide.ext.java.shared.dto.model.Type;
import org.eclipse.che.ide.ext.java.shared.dto.search.Match;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.ide.util.Pair;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Node represent package fragment.
 *
 * @author Evgen Vidolob
 */
public class PackageFragmentNode extends AbstractPresentationNode {


    private JavaResources            resources;
    private NodeFactory              nodeFactory;
    private PackageFragment          packageFragment;
    private Map<String, List<Match>> matches;
    private PackageFragmentRoot      parent;

    @Inject
    public PackageFragmentNode(JavaResources resources, NodeFactory nodeFactory, @Assisted PackageFragment packageFragment,
                               @Assisted Map<String, List<Match>> matches, @Assisted PackageFragmentRoot parent) {
        this.resources = resources;
        this.nodeFactory = nodeFactory;
        this.packageFragment = packageFragment;
        this.matches = matches;
        this.parent = parent;
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return PromiseHelper.newPromise(new AsyncPromiseHelper.RequestCall<List<Node>>() {
            @Override
            public void makeCall(AsyncCallback<List<Node>> callback) {
                List<Node> child = new ArrayList<Node>();
                if (packageFragment.getKind() == PackageFragmentRoot.K_SOURCE) {
                    for (CompilationUnit compilationUnit : packageFragment.getCompilationUnits()) {
                        for (Type type : compilationUnit.getTypes()) {
                            if (type.isPrimary()) {
                                child.add(nodeFactory.create(type, compilationUnit, null, matches));
                            }
                        }
                    }
                } else {
                    for (ClassFile classFile : packageFragment.getClassFiles()) {
                        child.add(nodeFactory.create(classFile.getType(), null, classFile, matches));
                    }
                }

                callback.onSuccess(child);
            }
        });
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        presentation.setPresentableText(packageFragment.getElementName());
        if (parent.getKind() == PackageFragmentRoot.K_BINARY) {
            presentation.setInfoText(parent.getElementName());
        } else {

            presentation.setInfoText(parent.getPath().substring(parent.getProjectPath().length() + 1));
        }
        presentation.setInfoTextWrapper(Pair.of("- ", ""));
        presentation.setPresentableIcon(resources.packageItem());
    }

    @Override
    public String getName() {
        return packageFragment.getElementName();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}
