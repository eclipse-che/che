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
package org.eclipse.che.ide.icon;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;

/**
 * Implementation of {@link IconRegistry}.
 *
 * @author Vitaly Parfonov
 * @author Artem Zatsarynnyi
 */
public class IconRegistryImpl implements IconRegistry {

  private Map<String, Icon> icons = new HashMap<>();

  @Override
  public void registerIcon(Icon icon) {
    icons.put(icon.getId(), icon);
  }

  @Override
  public Icon getIcon(String id) {
    Icon icon = icons.get(id);
    if (icon == null) {
      final String prefix = id.split("\\.")[0];
      final String defaultIconId = id.replaceFirst(prefix, "default");
      icon = icons.get(defaultIconId);
      if (icon == null) {
        icon = getGenericIcon();
      }
    }
    return icon;
  }

  @Override
  public Icon getIconIfExist(String id) {
    return icons.get(id);
  }

  @Override
  public Icon getGenericIcon() {
    return icons.get("default");
  }
}
