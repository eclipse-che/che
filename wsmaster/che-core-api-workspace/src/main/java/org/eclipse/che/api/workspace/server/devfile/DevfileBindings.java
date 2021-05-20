/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile;

import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;
import java.util.function.Consumer;
import org.eclipse.che.api.workspace.server.devfile.convert.component.ComponentToWorkspaceApplier;
import org.eclipse.che.api.workspace.server.devfile.validator.ComponentIntegrityValidator;

/**
 * Utility class to ease the adding of devfile-related bindings in a guice module configuration.
 *
 * <p>Consult the individual methods on this class to see if you need to use them in a particular
 * module.
 */
public class DevfileBindings {

  /**
   * The {@code binder} consumer can be used to bind {@link ComponentToWorkspaceApplier}
   * implementations to component types. I.e. the keys of the binder are component type strings and
   * the bindings are the deemed implementations.
   *
   * @param baseBinder the binder available in the Guice module calling this method
   * @param binder a consumer to accept a new binder for the workspace appliers
   */
  public static void onWorkspaceApplierBinder(
      Binder baseBinder, Consumer<MapBinder<String, ComponentToWorkspaceApplier>> binder) {
    binder.accept(
        MapBinder.newMapBinder(baseBinder, String.class, ComponentToWorkspaceApplier.class));
  }

  /**
   * The {@code binder} consumer can be used to bind {@link ComponentIntegrityValidator}
   * implementations to component types. I.e. the keys of the binder are component type strings and
   * the bindings are the deemed implementations.
   *
   * @param baseBinder the binder available in the Guice module calling this method
   * @param binder a consumer to accept a new binder for the component integrity validators
   */
  public static void onComponentIntegrityValidatorBinder(
      Binder baseBinder, Consumer<MapBinder<String, ComponentIntegrityValidator>> binder) {
    binder.accept(
        MapBinder.newMapBinder(baseBinder, String.class, ComponentIntegrityValidator.class));
  }

  private DevfileBindings() {}
}
