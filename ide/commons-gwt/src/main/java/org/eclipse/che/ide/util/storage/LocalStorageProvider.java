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
package org.eclipse.che.ide.util.storage;

import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;


/**
 * @author Anatoliy Bazko
 */
@Singleton
public interface LocalStorageProvider {
    
    /**
     * Returns {@link LocalStorage} if it is supported or null otherwise.
     */
    @Nullable
    LocalStorage get();
}
