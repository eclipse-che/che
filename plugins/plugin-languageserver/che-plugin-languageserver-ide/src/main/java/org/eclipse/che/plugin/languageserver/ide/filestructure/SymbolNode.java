/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide.filestructure;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.data.HasAction;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasNewPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NewNodePresentation;
import org.eclipse.che.jdt.ls.extension.api.dto.ExtendedSymbolInformation;
import org.eclipse.che.plugin.languageserver.ide.navigation.symbol.SymbolKindHelper;

/**
 * A node presenting {@link ExtendedSymbolInformation} objects
 *
 * @author Thomas Mäder
 */
public class SymbolNode extends AbstractTreeNode implements HasNewPresentation, HasAction {
  private SymbolKindHelper symbolHelper;
  private PromiseProvider promiseProvider;
  private ElementSelectionDelegate<ExtendedSymbolInformation> delegate;
  private ExtendedSymbolInformation symbol;

  @Inject
  public SymbolNode(
      SymbolKindHelper symbolHelper,
      PromiseProvider promiseProvider,
      @Assisted ElementSelectionDelegate<ExtendedSymbolInformation> delegate,
      @Assisted ExtendedSymbolInformation symbol) {
    this.symbolHelper = symbolHelper;
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
            .map(child -> new SymbolNode(symbolHelper, promiseProvider, delegate, child))
            .collect(Collectors.toList()));
  }

  @Override
  public NewNodePresentation getPresentation() {
    String name = symbol.getInfo().getName();
    return new NewNodePresentation.Builder()
        .withIcon(symbolHelper.getIcon(symbol.getInfo().getKind()))
        .withNodeText(name)
        .build();
  }

  @Override
  public void actionPerformed() {
    delegate.onSelect(symbol);
  }
}
