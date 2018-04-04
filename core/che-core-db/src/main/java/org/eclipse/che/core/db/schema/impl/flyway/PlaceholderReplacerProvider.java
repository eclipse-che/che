/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
