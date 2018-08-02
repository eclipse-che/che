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
import org.eclipse.che.ide.editor.orion.client.OrionOccurrencesRegistrant;
import org.eclipse.che.plugin.languageserver.ide.highlighting.OccurrencesProvider;

/** @author Dmytro Kulieshov */
@Singleton
public class DefaultOccurrencesProviderInitializer {

  private final DefaultExtensionToMimeTypeMappings defaultExtensionToMimeTypeMappings;
  private final OrionOccurrencesRegistrant orionOccurrencesRegistrant;
  private final OccurrencesProvider occurrencesProvider;

  @Inject
  public DefaultOccurrencesProviderInitializer(
      DefaultExtensionToMimeTypeMappings defaultExtensionToMimeTypeMappings,
      OrionOccurrencesRegistrant orionOccurrencesRegistrant,
      OccurrencesProvider occurrencesProvider) {
    this.defaultExtensionToMimeTypeMappings = defaultExtensionToMimeTypeMappings;
    this.orionOccurrencesRegistrant = orionOccurrencesRegistrant;
    this.occurrencesProvider = occurrencesProvider;
  }

  void initialize() {
    JsArrayString contentTypes = JsArrayString.createArray().cast();
    defaultExtensionToMimeTypeMappings.getMimeTypes().forEach(contentTypes::push);
    orionOccurrencesRegistrant.registerOccurrencesHandler(contentTypes, occurrencesProvider);
  }
}
