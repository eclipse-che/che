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
package org.eclipse.che.ide.ext.java.client.tree;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.resources.Resource.FILE;

import com.google.common.annotations.Beta;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.resources.tree.ContainerNode;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.NodeInterceptor;
import org.eclipse.che.ide.ui.smartTree.data.settings.SettingsProvider;

/** @author Vlad Zhukovskiy */
@Beta
@Singleton
public class JavaPackageConnector implements NodeInterceptor {

  private PromiseProvider promises;
  private final JavaNodeFactory nodeFactory;
  private final SettingsProvider settingsProvider;

  @Inject
  public JavaPackageConnector(
      PromiseProvider promises, JavaNodeFactory nodeFactory, SettingsProvider settingsProvider) {
    this.promises = promises;
    this.nodeFactory = nodeFactory;
    this.settingsProvider = settingsProvider;
  }

  @Override
  public Promise<List<Node>> intercept(Node parent, List<Node> children) {
    if (parent instanceof ResourceNode) {
      return transform((ResourceNode) parent, children);
    } else {
      return promises.resolve(children);
    }
  }

  @Override
  public int getPriority() {
    return NORM_PRIORITY;
  }

  protected Promise<List<Node>> transform(ResourceNode parent, final List<Node> children) {

    if (!isSourceOrPackage(parent)) {
      return promises.resolve(children);
    }

    if (children.size() == 1) {
      final Node node = children.get(0);

      if (node instanceof ContainerNode) {
        return getNonEmptyPackage(((ContainerNode) node).getData())
            .then(
                new Function<Container, List<Node>>() {
                  @Override
                  public List<Node> apply(Container pkg) throws FunctionException {
                    final Node packageNode =
                        nodeFactory.newPackage(pkg, settingsProvider.getSettings());

                    return singletonList(packageNode);
                  }
                });
      } else {
        return promises.resolve(children);
      }

    } else if (children.size() > 1) {
      final Node[] nodes = new Node[children.size()];
      Promise[] pkgPromises = new Promise[0];

      for (int i = 0; i < children.size(); i++) {
        final Node node = children.get(i);
        final int index = i;

        if (node instanceof ContainerNode) {
          int pkgIndex = pkgPromises.length;
          pkgPromises = Arrays.copyOf(pkgPromises, pkgIndex + 1);
          pkgPromises[pkgIndex] =
              getNonEmptyPackage(((ContainerNode) node).getData())
                  .then(
                      new Operation<Container>() {
                        @Override
                        public void apply(Container pkg) throws OperationException {
                          nodes[index] =
                              nodeFactory.newPackage(pkg, settingsProvider.getSettings());
                        }
                      });
        } else {
          nodes[index] = node;
        }
      }

      return promises
          .all(pkgPromises)
          .then(
              new Function<JsArrayMixed, List<Node>>() {
                @Override
                public List<Node> apply(JsArrayMixed ignored) throws FunctionException {
                  return Arrays.asList(nodes);
                }
              });
    }

    return promises.resolve(children);
  }

  protected boolean isSourceOrPackage(ResourceNode node) {
    return node.getData().getParentWithMarker(SourceFolderMarker.ID).isPresent();
  }

  protected Promise<Container> getNonEmptyPackage(final Container source) {
    return source
        .getChildren()
        .thenPromise(
            new Function<Resource[], Promise<Container>>() {
              @Override
              public Promise<Container> apply(Resource[] children) throws FunctionException {

                if (children == null || children.length == 0 || children.length > 1) {
                  return promises.resolve(source);
                } else {
                  final Resource resource = children[0];

                  if (resource.getResourceType() == FILE) {
                    return promises.resolve(source);
                  } else {
                    return getNonEmptyPackage((Container) resource);
                  }
                }
              }
            })
        .catchErrorPromise(
            new Function<PromiseError, Promise<Container>>() {
              @Override
              public Promise<Container> apply(PromiseError arg) throws FunctionException {
                return promises.resolve(source);
              }
            });
  }
}
