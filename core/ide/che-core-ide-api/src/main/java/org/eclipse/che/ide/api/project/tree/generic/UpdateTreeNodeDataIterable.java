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
package org.eclipse.che.ide.api.project.tree.generic;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * Interface uses for update TreeNode data after changing TreeNode path
 *
 * @author Alexander Andrienko
 */
@Deprecated
public interface UpdateTreeNodeDataIterable {
    /**
     * Takes away new node data from server, update node and launch action in callBack
     * @param callback callback with some action
     * @param newPath new TreeNode path
     */
    void updateData(AsyncCallback<Void> callback, String newPath);
}
