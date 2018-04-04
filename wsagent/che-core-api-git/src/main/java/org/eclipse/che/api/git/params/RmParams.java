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
