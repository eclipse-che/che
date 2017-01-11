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
package org.eclipse.che.ide.ui.smartTree;

import com.google.gwt.view.client.ProvidesKey;

/**
 * ModelKeyProviders are responsible for returning a unique key for a given
 * model.
 *
 * @param <T>
 *         the model type
 * @author Vlad Zhukovskyi
 */
public interface UniqueKeyProvider<T> extends ProvidesKey<T> {

    /**
     * Gets a non-null key value that maps to this object. Keys must be consistent and
     * unique for a given model, as a database primary key would be used.
     *
     * @return non-null {@link String} key for {@link T} object
     */
    String getKey(T item);
}