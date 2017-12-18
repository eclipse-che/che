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

/** Class contains an information about result of the text search operation. */
public class SearchResult {
  private List<SearchItemReference> itemReferences;
  private long totalHits;

  public SearchResult(List<SearchItemReference> itemReferences, long totalHits) {
    this.itemReferences = itemReferences;
    this.totalHits = totalHits;
  }

  /** returns list of found items {@link SearchItemReference} */
  public List<SearchItemReference> getItemReferences() {
    return itemReferences;
  }

  /** returns total file count where requested text was found */
  public long getTotalHits() {
    return totalHits;
  }
}
