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
package org.eclipse.che.ide.part.explorer.project.macro;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.resources.tree.ResourceNode;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

/**
 * Provider which is responsible for retrieving the resource name from the project explorer.
 * <p>
 * Macro provided: <code>${explorer.current.file.name}</code>
 * <p>
 * In case if project explorer has more than one selected file, comma separated file list is returned.
 *
 * @author Vlad Zhukovskyi
 * @see Macro
 * @see ProjectExplorerPresenter
 * @since 4.7.0
 */
@Beta
@Singleton
public class ExplorerCurrentFileNameMacro implements Macro {

    public static final String KEY = "${explorer.current.file.name}";

    private Predicate<Node> resNodePredicate = new Predicate<Node>() {
        @Override
        public boolean apply(@Nullable Node input) {
            checkNotNull(input);

            return input instanceof ResourceNode;
        }
    };

    private Function<Node, Resource> nodeToResourceFun = new Function<Node, Resource>() {
        @Nullable
        @Override
        public Resource apply(@Nullable Node input) {
            checkNotNull(input);
            checkState(input instanceof ResourceNode);

            return ((ResourceNode)input).getData();
        }
    };

    private Function<Resource, String> resourceToNameFun = new Function<Resource, String>() {
        @Nullable
        @Override
        public String apply(@Nullable Resource input) {
            checkNotNull(input);

            return input.getName();
        }
    };

    private ProjectExplorerPresenter projectExplorer;
    private PromiseProvider          promises;
    private final CoreLocalizationConstant localizationConstants;

    @Inject
    public ExplorerCurrentFileNameMacro(ProjectExplorerPresenter projectExplorer,
                                        PromiseProvider promises,
                                        CoreLocalizationConstant localizationConstants) {
        this.projectExplorer = projectExplorer;
        this.promises = promises;
        this.localizationConstants = localizationConstants;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return KEY;
    }

    @Override
    public String getDescription() {
        return localizationConstants.macroExplorerCurrentFileNameDescription();
    }

    /** {@inheritDoc} */
    @Override
    public Promise<String> expand() {

        List<Node> selectedNodes = projectExplorer.getTree().getSelectionModel().getSelectedNodes();

        if (selectedNodes.isEmpty()) {
            return promises.resolve("");
        }

        final Iterable<Resource> resources = transform(filter(selectedNodes, resNodePredicate), nodeToResourceFun);
        final String commaSeparated = Joiner.on(", ").join(transform(resources, resourceToNameFun));

        return promises.resolve(commaSeparated);
    }
}
