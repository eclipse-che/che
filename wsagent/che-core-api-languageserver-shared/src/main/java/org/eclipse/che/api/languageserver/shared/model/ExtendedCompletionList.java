/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
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
