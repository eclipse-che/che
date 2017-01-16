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
package org.eclipse.che.ide.editor.orion.client;

import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;

/**
 * Holds promise that resolve when all editor js scripts are loaded ad initialized
 */
@Singleton
public class EditorInitializePromiseHolder {

    private Promise<Void> initializerPromise;

    public void setInitializerPromise(Promise<Void> initializerPromise) {
        this.initializerPromise = initializerPromise;
    }

    public Promise<Void> getInitializerPromise() {
        if (initializerPromise == null) {
            throw new RuntimeException("Editor initializer not set");
        }
        return initializerPromise;
    }
}
