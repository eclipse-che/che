/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver.shared.model;

import java.util.List;
import org.eclipse.lsp4j.CompletionList;

/**
 * Version of {@link CompletionList} that holds {@link ExtendedCompletionItem}s
 *
 * @author Thomas Mäder
 */
public class ExtendedCompletionList {
  private boolean inComplete;
  private List<ExtendedCompletionItem> items;

  public ExtendedCompletionList(boolean incomplete, List<ExtendedCompletionItem> items) {
    this.inComplete = incomplete;
    this.items = items;
  }

  public ExtendedCompletionList() {}

  public List<ExtendedCompletionItem> getItems() {
    return items;
  }

  public void setItems(List<ExtendedCompletionItem> items) {
    this.items = items;
  }

  public boolean isInComplete() {
    return inComplete;
  }

  public void setInComplete(boolean inComplete) {
    this.inComplete = inComplete;
  }
}
