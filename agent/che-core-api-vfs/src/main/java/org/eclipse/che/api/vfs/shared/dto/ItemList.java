/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.shared.dto;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Set of abstract items for paging view.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 */
@DTO
public interface ItemList {
    /** @return set of items */
    List<Item> getItems();

    ItemList withItems(List<Item> list);

    void setItems(List<Item> list);

    /**
     * @return total number of items. It is not need to be equals to number of items in current list {@link #getItems()}.
     *         It may be equals to number of items in current list only if this list contains all requested items and no
     *         more pages available. This method must return -1 if total number of items in unknown.
     */
    int getNumItems();

    ItemList withNumItems(int numItems);

    void setNumItems(int numItems);

    /** @return <code>false</code> if this is last sub-set of items in paging */
    boolean isHasMoreItems();

    ItemList withHasMoreItems(boolean hasMoreItems);

    void setHasMoreItems(boolean hasMoreItems);
}
