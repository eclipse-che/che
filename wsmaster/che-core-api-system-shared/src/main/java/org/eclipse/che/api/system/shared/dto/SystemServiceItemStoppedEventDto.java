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
package org.eclipse.che.api.system.shared.dto;

import org.eclipse.che.api.system.shared.event.EventType;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.shared.DTO;

/**
 * See {@link EventType#SERVICE_ITEM_STOPPED} for details.
 *
 * @author Yevhenii Voevodin
 */
@DTO
public interface SystemServiceItemStoppedEventDto extends SystemServiceEventDto {

  /** Returns an item for which this event is published(like workspace id). */
  String getItem();

  void setItem(String item);

  SystemServiceItemStoppedEventDto withItem(String item);

  /**
   * Returns an amount of items currently stopped, it's either present with {@link #getTotal()} or
   * missing at all(null is returned).
   */
  @Nullable
  Integer getCurrent();

  void setCurrent(Integer current);

  SystemServiceItemStoppedEventDto withCurrent(Integer current);

  /**
   * Returns total count of items which had not been stopped before service shutdown was called.
   * It's either present with {@link #getCurrent()} or missing at all(null is returned).
   */
  @Nullable
  Integer getTotal();

  void setTotal(Integer total);

  SystemServiceItemStoppedEventDto withTotal(Integer total);
}
