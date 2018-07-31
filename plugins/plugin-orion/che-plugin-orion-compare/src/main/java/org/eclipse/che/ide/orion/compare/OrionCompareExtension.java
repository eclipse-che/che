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
package org.eclipse.che.ide.orion.compare;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.extension.Extension;

/** @author Evgen Vidolob */
@Extension(title = "Orion Compare", version = "1.0.0")
@Singleton
public class OrionCompareExtension {

  @Inject
  public OrionCompareExtension() {}
}
