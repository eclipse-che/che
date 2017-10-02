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
package org.eclipse.che.ide.api.resources;

import java.util.List;

public class SearchResult {
  private List<SearchItemReference> itemReferences;
  private int totalHits;

  public SearchResult(List<SearchItemReference> itemReferences, int totalHits) {
    this.itemReferences = itemReferences;
    this.totalHits = totalHits;
  }

  public List<SearchItemReference> getItemReferences() {
    return itemReferences;
  }

  public int getTotalHits() {
    return totalHits;
  }
}
