/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.statepersistance;

import elemental.json.JsonObject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;

/**
 * Defines requirements for a component which would like to persist some state of workspace across
 * sessions.
 *
 * <p>Implementations of this interface need to be registered using a multibinder in order to be
 * picked-up on IDE start-up:
 *
 * <p><code>
 * GinMultibinder<StateComponent> stateComponents = GinMultibinder.newSetBinder(binder(), StateComponent.class);
 * stateComponents.addBinding().to(Foo.class);
 * </code>
 *
 * @author Evgen Vidolob
 * @author Vlad Zhukovskyi
 */
public interface StateComponent {

  /**
   * The minimum priority that state component can have.
   *
   * @see #getPriority()
   * @since 5.16.0
   */
  int MIN_PRIORITY = 1;

  /**
   * The default priority that is assigned to a state component.
   *
   * @see #getPriority()
   * @since 5.16.0
   */
  int DEFAULT_PRIORITY = 5;

  /**
   * The maximum priority that state component can have.
   *
   * @see #getPriority()
   * @since 5.16.0
   */
  int MAX_PRIORITY = 10;

  /**
   * Identifier of the component which may have persistent state. Usually uses to identify from the
   * raw json.
   *
   * @return component id, any string value, non-null and non-empty. If null occurred, then
   *     component is not take a part in serialization
   * @since 5.16.0
   */
  String getId();

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
  Promise<Void> loadState(@NotNull JsonObject state);

  /**
   * Priority of the execution. Each component may be prioritized to execute one self. Values should
   * be from 1 (the last one to execute) to 10 (should be executed as first). Default value is 5.
   *
   * @return priority for the interceptor in which it should be run
   * @see #MIN_PRIORITY
   * @see #DEFAULT_PRIORITY
   * @see #MAX_PRIORITY
   * @since 5.16.0
   */
  default int getPriority() {
    return DEFAULT_PRIORITY;
  }
}
