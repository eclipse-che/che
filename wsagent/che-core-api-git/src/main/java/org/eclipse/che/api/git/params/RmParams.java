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
package org.eclipse.che.api.git.params;

import java.util.ArrayList;
import java.util.List;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#rm(RmParams)}.
 *
 * @author Igor Vinokur
 */
public class RmParams {

  private List<String> items;
  private boolean cached;

  private RmParams() {}

  /**
   * Create new {@link RmParams} instance.
   *
   * @param items files to remove
   */
  public static RmParams create(List<String> items) {
    return new RmParams().withItems(items);
  }

  /** Returns files to remove */
  public List<String> getItems() {
    return items == null ? new ArrayList<>() : items;
  }

  public RmParams withItems(List<String> items) {
    this.items = items;
    return this;
  }

  /** Returns {@code true} if needed to remove from index only, otherwise returns {@code false}. */
  public boolean isCached() {
    return cached;
  }

  public RmParams withCached(boolean cached) {
    this.cached = cached;
    return this;
  }
}
