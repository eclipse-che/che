/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.api.distributed.cache;

import java.util.List;
import java.util.Map;
import org.jgroups.View;
import org.jgroups.blocks.ReplicatedHashMap;

/**
 * <b>Purpose</b>: Provides an empty implementation of {@link ReplicatedHashMap.Notification}. Users
 * who do not require to be notified about all map changes can subclass this class and implement
 * only the methods required.
 *
 * @author Sergii Leshchenko
 */
public class ReplicatedMapNotificationAdapter implements ReplicatedHashMap.Notification {
  @Override
  public void entrySet(Object key, Object value) {}

  @Override
  public void entryRemoved(Object key) {}

  @Override
  public void contentsSet(Map new_entries) {}

  @Override
  public void contentsCleared() {}

  @Override
  public void viewChange(View view, List mbrs_joined, List mbrs_left) {}
}
