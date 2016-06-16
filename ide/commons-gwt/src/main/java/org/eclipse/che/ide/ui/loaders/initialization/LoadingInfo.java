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
package org.eclipse.che.ide.ui.loaders.initialization;

import java.util.List;

import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status;

/**
 * Contains information about the operations of loading.
 *
 * @author Roman Nikitenko
 */
public interface LoadingInfo {

    /**
     * @return the list of operations required for the current process of loading
     */
    List<OperationInfo> getOperations();

    /**
     * @return the list of display name for operations required for the current process of loading
     */
    List<String> getDisplayNames();

    /**
     * Sets the status for the operation
     *
     * @param operationName
     *         display name of the operation
     * @param status
     *         the status to setting
     */
    void setOperationStatus(String operationName, Status status);
}
