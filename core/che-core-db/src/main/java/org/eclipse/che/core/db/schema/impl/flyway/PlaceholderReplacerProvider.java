/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.core.db.schema.impl.flyway;

import javax.inject.Inject;
import javax.inject.Provider;
import org.eclipse.che.inject.ConfigurationProperties;
import org.flywaydb.core.internal.util.PlaceholderReplacer;

/**
 * Placeholder replacer that uses configuration properties.
 *
 * @author Yevhenii Voevodin
 */
public class PlaceholderReplacerProvider implements Provider<PlaceholderReplacer> {

  private final PlaceholderReplacer replacer;

  @Inject
  public PlaceholderReplacerProvider(ConfigurationProperties properties) {
    replacer = new PlaceholderReplacer(properties.getProperties(".*"), "${", "}");
  }

  @Override
  public PlaceholderReplacer get() {
    return replacer;
  }
}
