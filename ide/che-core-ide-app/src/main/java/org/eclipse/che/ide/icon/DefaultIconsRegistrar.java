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
package org.eclipse.che.ide.icon;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;

/** @author Evgen Vidolob */
@Singleton
public class DefaultIconsRegistrar {

  @Inject
  public DefaultIconsRegistrar(IconRegistry iconRegistry, Resources resources) {
    iconRegistry.registerIcon(
        new Icon(
            "default.projecttype.small.icon", "default/project.png", resources.defaultProject()));

    iconRegistry.registerIcon(
        new Icon("default.folder.small.icon", "default/folder.png", resources.defaultFolder()));

    iconRegistry.registerIcon(
        new Icon("default.file.small.icon", "default/file.png", resources.defaultFile()));

    iconRegistry.registerIcon(new Icon("default", "default/default.jpg", resources.defaultIcon()));
  }
}
