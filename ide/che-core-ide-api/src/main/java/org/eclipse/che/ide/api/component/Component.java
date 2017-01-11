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
package org.eclipse.che.ide.api.component;

import com.google.gwt.core.client.Callback;

/**
 * Components that have to be started on application's startup
 * must implement this interface.
 * All components are registered via gin map binding.
 * For example, if you have YourComponent class, implementation of this interface, you must write in your GinModule this:
 * <code>
 * GinMapBinder<String, Component> mapBinder =
 * GinMapBinder.newMapBinder(binder(), String.class, Component.class);
 * mapBinder.addBinding("your key").to(YourComponent.class);
 * </code>
 *
 * @author Nikolay Zamosenchuk
 * @author Evgen Vidolob
 */
public interface Component {

    /**
     * Starts Component.
     * Must do not throw any exceptions.
     * All exceptions must be passed in {@code Callback.onFailure(java.lang.Exception)}
     */
    void start(Callback<Component, Exception> callback);
}
