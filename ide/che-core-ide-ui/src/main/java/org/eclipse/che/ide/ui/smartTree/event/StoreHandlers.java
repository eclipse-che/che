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
package org.eclipse.che.ide.ui.smartTree.event;

import com.google.gwt.event.shared.HandlerRegistration;
import org.eclipse.che.ide.ui.smartTree.event.StoreAddEvent.HasStoreAddHandlers;
import org.eclipse.che.ide.ui.smartTree.event.StoreAddEvent.StoreAddHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreClearEvent.HasStoreClearHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreClearEvent.StoreClearHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreDataChangeEvent.HasStoreDataChangeHandlers;
import org.eclipse.che.ide.ui.smartTree.event.StoreDataChangeEvent.StoreDataChangeHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreRecordChangeEvent.HasStoreRecordChangeHandlers;
import org.eclipse.che.ide.ui.smartTree.event.StoreRecordChangeEvent.StoreRecordChangeHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreRemoveEvent.HasStoreRemoveHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreRemoveEvent.StoreRemoveHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreSortEvent.HasStoreSortHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreSortEvent.StoreSortHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreUpdateEvent.HasStoreUpdateHandlers;
import org.eclipse.che.ide.ui.smartTree.event.StoreUpdateEvent.StoreUpdateHandler;

/**
 * Aggregating handler interface for {@link StoreAddHandler}, {@link
 * org.eclipse.che.ide.ui.smartTree.event.StoreRemoveEvent.StoreRemoveHandler}, {@link
 * org.eclipse.che.ide.ui.smartTree.event.StoreClearEvent.StoreClearHandler}, {@link
 * org.eclipse.che.ide.ui.smartTree.event.StoreUpdateEvent.StoreUpdateHandler}, {@link
 * org.eclipse.che.ide.ui.smartTree.event.StoreDataChangeEvent.StoreDataChangeHandler}, {@link
 * org.eclipse.che.ide.ui.smartTree.event.StoreRecordChangeEvent.StoreRecordChangeHandler}, {@link
 * org.eclipse.che.ide.ui.smartTree.event.StoreSortEvent.StoreSortHandler}.
 *
 * @author Vlad Zhukovskiy
 */
public interface StoreHandlers
    extends StoreAddHandler,
        StoreRemoveHandler,
        StoreClearHandler,
        StoreUpdateHandler,
        StoreDataChangeHandler,
        StoreRecordChangeHandler,
        StoreSortHandler {

  public interface HasStoreHandlers
      extends HasStoreAddHandlers,
          HasStoreRemoveHandler,
          HasStoreUpdateHandlers,
          HasStoreRecordChangeHandlers,
          HasStoreClearHandler,
          HasStoreDataChangeHandlers,
          HasStoreSortHandler {
    HandlerRegistration addStoreHandlers(StoreHandlers handlers);
  }
}
