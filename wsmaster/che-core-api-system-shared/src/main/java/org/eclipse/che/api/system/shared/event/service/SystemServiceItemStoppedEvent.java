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
package org.eclipse.che.api.system.shared.event.service;

import java.util.Objects;
import org.eclipse.che.api.system.shared.event.EventType;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * See {@link EventType#SERVICE_ITEM_STOPPED} description.
 *
 * @author Yevhenii Voevodin
 */
public class SystemServiceItemStoppedEvent extends SystemServiceEvent {

  private final String item;

  private Integer total;
  private Integer current;

  public SystemServiceItemStoppedEvent(String serviceName, String item) {
    super(serviceName);
    this.item = Objects.requireNonNull(item, "Item required");
  }

  public SystemServiceItemStoppedEvent(
      String serviceName, String item, @Nullable Integer current, @Nullable Integer total) {
    this(serviceName, item);
    this.current = current;
    this.total = total;
  }

  @Override
  public EventType getType() {
    return EventType.SERVICE_ITEM_STOPPED;
  }

  public String getItem() {
    return item;
  }

  @Nullable
  public Integer getTotal() {
    return total;
  }

  @Nullable
  public Integer getCurrent() {
    return current;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SystemServiceItemStoppedEvent)) {
      return false;
    }
    final SystemServiceItemStoppedEvent that = (SystemServiceItemStoppedEvent) obj;
    return super.equals(that)
        && item.equals(that.item)
        && Objects.equals(total, that.total)
        && Objects.equals(current, that.current);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + super.hashCode();
    hash = 31 * hash + item.hashCode();
    hash = 31 * hash + Objects.hashCode(total);
    hash = 31 * hash + Objects.hashCode(current);
    return hash;
  }

  @Override
  public String toString() {
    return "SystemServiceItemStoppedEvent{"
        + "item='"
        + item
        + '\''
        + ", total="
        + total
        + ", current="
        + current
        + ", eventType='"
        + getType()
        + '\''
        + ", service='"
        + getServiceName()
        + "\'}";
  }
}
