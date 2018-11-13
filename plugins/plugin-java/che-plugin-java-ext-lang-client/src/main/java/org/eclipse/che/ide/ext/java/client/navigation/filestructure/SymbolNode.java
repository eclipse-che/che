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
package org.eclipse.che.ide.ext.java.client.navigation.filestructure;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.ext.java.client.util.SymbolIcons;
import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.data.HasAction;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasNewPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NewNodePresentation;
import org.eclipse.che.jdt.ls.extension.api.dto.ExtendedSymbolInformation;

/**
 * A node presenting {@link ExtendedSymbolInformation} objects
 *
 * @author Thomas Mäder
 */
public class SymbolNode extends AbstractTreeNode implements HasNewPresentation, HasAction {
  private SymbolIcons symbolIcons;
  private PromiseProvider promiseProvider;
  private ElementSelectionDelegate<ExtendedSymbolInformation> delegate;
  private ExtendedSymbolInformation symbol;

  @Inject
  public SymbolNode(
      SymbolIcons symbolIcons,
      PromiseProvider promiseProvider,
      @Assisted ElementSelectionDelegate<ExtendedSymbolInformation> delegate,
      @Assisted ExtendedSymbolInformation symbol) {
    this.symbolIcons = symbolIcons;
    this.promiseProvider = promiseProvider;
    this.delegate = delegate;
    this.symbol = symbol;
  }

  @Override
  public String getName() {
    return symbol.getInfo().getName();
  }

  @Override
  public boolean isLeaf() {
    return symbol.getChildren().isEmpty();
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return promiseProvider.resolve(
        symbol
            .getChildren()
            .stream()
            .map(child -> new SymbolNode(symbolIcons, promiseProvider, delegate, child))
            .collect(Collectors.toList()));
  }

  @Override
  public NewNodePresentation getPresentation() {
    String name = symbol.getInfo().getName();
    return new NewNodePresentation.Builder()
        .withIcon(symbolIcons.get(symbol))
        .withNodeText(name)
        .build();
  }

  @Override
  public void actionPerformed() {
    delegate.onSelect(symbol);
  }
}
