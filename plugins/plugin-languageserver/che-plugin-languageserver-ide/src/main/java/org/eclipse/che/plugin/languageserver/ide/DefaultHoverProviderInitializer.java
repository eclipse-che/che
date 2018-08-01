/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide;

import com.google.gwt.core.client.JsArrayString;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.editor.filetype.DefaultExtensionToMimeTypeMappings;
import org.eclipse.che.ide.editor.orion.client.OrionHoverRegistrant;
import org.eclipse.che.plugin.languageserver.ide.hover.HoverProvider;

/** @author Dmytro Kulieshov */
@Singleton
public class DefaultHoverProviderInitializer {

  private final DefaultExtensionToMimeTypeMappings defaultExtensionToMimeTypeMappings;
  private final OrionHoverRegistrant orionHoverRegistrant;
  private final HoverProvider hoverProvider;

  @Inject
  public DefaultHoverProviderInitializer(
      DefaultExtensionToMimeTypeMappings defaultExtensionToMimeTypeMappings,
      OrionHoverRegistrant orionHoverRegistrant,
      HoverProvider hoverProvider) {
    this.defaultExtensionToMimeTypeMappings = defaultExtensionToMimeTypeMappings;
    this.orionHoverRegistrant = orionHoverRegistrant;
    this.hoverProvider = hoverProvider;
  }

  void initialize() {
    JsArrayString contentTypes = JsArrayString.createArray().cast();
    defaultExtensionToMimeTypeMappings.getMimeTypes().forEach(contentTypes::push);
    orionHoverRegistrant.registerHover(contentTypes, hoverProvider);
  }
}
