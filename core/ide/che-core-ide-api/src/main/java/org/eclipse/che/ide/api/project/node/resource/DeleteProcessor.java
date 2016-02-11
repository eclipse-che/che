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
package org.eclipse.che.ide.api.project.node.resource;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.project.node.HasDataObject;

import javax.validation.constraints.NotNull;

/**
 * Provide mechanism to allow physically removing data object during node deletion.
 *
 * @author Vlad Zhukovskiy
 */
public interface DeleteProcessor<DataObject> {
    /**
     * Delete data object stored in node.
     *
     * @param node
     *         data store node
     * @return promise with deleted data object
     */
    Promise<DataObject> delete(@NotNull HasDataObject<DataObject> node);
}
