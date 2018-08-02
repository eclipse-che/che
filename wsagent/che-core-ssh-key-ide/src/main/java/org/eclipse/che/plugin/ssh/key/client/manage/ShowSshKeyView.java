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
package org.eclipse.che.plugin.ssh.key.client.manage;

import com.google.inject.ImplementedBy;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.mvp.View;

/** @author Dmitry Shnurenko */
@ImplementedBy(ShowSshKeyViewImpl.class)
public interface ShowSshKeyView extends View<ShowSshKeyView.ActionDelegate> {

  /**
   * The method displays 'show reference' dialog with passed parameters.
   *
   * @param name of service
   * @param key content of key
   */
  void show(@NotNull String name, @NotNull String key);

  interface ActionDelegate {}
}
