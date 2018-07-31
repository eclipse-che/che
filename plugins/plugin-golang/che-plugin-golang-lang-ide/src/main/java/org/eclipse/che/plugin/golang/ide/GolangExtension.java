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
package org.eclipse.che.plugin.golang.ide;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;

/** @author Eugene Ivantsov */
@Extension(title = "Golang")
public class GolangExtension {

  public static final String GOLANG_CATEGORY = "Golang";

  @Inject
  private void prepareActions(GolangResources resources, IconRegistry iconRegistry) {
    iconRegistry.registerIcon(
        new Icon(GOLANG_CATEGORY + ".samples.category.icon", resources.golangIcon()));
  }
}
