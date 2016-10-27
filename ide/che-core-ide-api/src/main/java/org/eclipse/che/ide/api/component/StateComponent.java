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
package org.eclipse.che.ide.api.component;

import elemental.json.JsonObject;

import javax.validation.constraints.NotNull;

/**
 * Defines requirements for a component which would like to persist some state of workspace across sessions.
 * <p/>Implementations of this interface need to be registered using
 * a multibinder in order to be picked-up on IDE start-up:
 * <p>
 * <code>
 *     GinMapBinder<String, StateComponent> stateComponents = GinMapBinder.newMapBinder(binder(), String.class, StateComponent.class);
 *     stateComponents.addBinding("foo").to(Foo.class);
 * </code>
 * </p>
 * @author Evgen Vidolob
 */
public interface StateComponent {

    /**
     * Called when component should store his state.
     *
     * @return the JSON object that represent state of the component.
     */
    @NotNull
    JsonObject getState();

    /**
     * Called when component should restore his state.
     *
     * @param state the component state object
     */
    void loadState(@NotNull JsonObject state);

}
