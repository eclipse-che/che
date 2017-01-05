/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
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
 * Aggregating handler interface for {@link StoreAddHandler}, {@link org.eclipse.che.ide.ui.smartTree.event.StoreRemoveEvent.StoreRemoveHandler}, {@link org.eclipse.che.ide.ui.smartTree.event.StoreClearEvent.StoreClearHandler},
 * {@link org.eclipse.che.ide.ui.smartTree.event.StoreUpdateEvent.StoreUpdateHandler}, {@link org.eclipse.che.ide.ui.smartTree.event.StoreDataChangeEvent.StoreDataChangeHandler}, {@link org.eclipse.che.ide.ui.smartTree.event.StoreRecordChangeEvent.StoreRecordChangeHandler}, {@link org.eclipse.che.ide.ui.smartTree.event.StoreSortEvent.StoreSortHandler}.
 *
 * @author Vlad Zhukovskiy
 */
public interface StoreHandlers extends StoreAddHandler, StoreRemoveHandler, StoreClearHandler, StoreUpdateHandler, StoreDataChangeHandler,
                                       StoreRecordChangeHandler, StoreSortHandler {

    public interface HasStoreHandlers extends HasStoreAddHandlers, HasStoreRemoveHandler, HasStoreUpdateHandlers,
                                              HasStoreRecordChangeHandlers, HasStoreClearHandler, HasStoreDataChangeHandlers,
                                              HasStoreSortHandler {
        HandlerRegistration addStoreHandlers(StoreHandlers handlers);
    }
}