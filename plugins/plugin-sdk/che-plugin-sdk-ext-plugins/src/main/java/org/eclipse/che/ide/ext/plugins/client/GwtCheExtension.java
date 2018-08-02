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
package org.eclipse.che.ide.ext.plugins.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.extension.Extension;

/**
 * Entry point for an extension that adds support to work with GWT SDM in Che projects.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
@Extension(title = "GWT support for Che", version = "1.0.0")
public class GwtCheExtension {

  @Inject
  public GwtCheExtension() {}
}
