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
package org.eclipse.che.ide.api.keybinding;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.extension.SDK;
import org.eclipse.che.ide.util.input.CharCodeWithModifiers;

/**
 * Public interface of the key binding management. The key binding defines the key sequence that
 * should be used to invoke the command. A key binding may reference a scheme which is used to group
 * key bindings into different named schemes that the user may activate.
 *
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @version $Id:
 */
@SDK(title = "ide.api.ui.keyBinding")
public interface KeyBindingAgent {

  /**
   * Global scheme, bindings added in this scheme always
   *
   * @return
   */
  Scheme getGlobal();

  /**
   * Get build in Eclipse key binding scheme.
   *
   * @deprecated Eclipse should not be accessed like that, use {@link
   *     KeyBindingAgent#getScheme(String)} or {@link KeyBindingAgent#getActive()}
   * @return the Eclipse scheme.
   */
  @Deprecated
  Scheme getEclipse();

  /**
   * Currently active scheme.
   *
   * @return the scheme
   */
  Scheme getActive();

  /**
   * Register a new scheme
   *
   * @param scheme to register
   */
  void addScheme(Scheme scheme);

  /**
   * Get a registered Scheme, and return null if nothing is corresponding.
   *
   * @param id of the registered scheme
   * @return the expected scheme, or null.
   */
  Scheme getScheme(String id);

  /** List registered schemes */
  List<Scheme> getSchemes();

  /** Change active scheme using his identifier */
  void setActive(@NotNull String scheme);

  /**
   * @return keyboard shortcut for the action with the specified <code>actionId</code> or an null if
   *     the action doesn't have any keyboard shortcut.
   */
  @Nullable
  CharCodeWithModifiers getKeyBinding(@NotNull String actionId);
}
