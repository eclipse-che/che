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
package org.eclipse.che.ide.reference;

import com.google.inject.ImplementedBy;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.resource.Path;

/** @author Dmitry Shnurenko */
@ImplementedBy(ShowReferenceViewImpl.class)
interface ShowReferenceView extends View<ShowReferenceView.ActionDelegate> {

  /**
   * The method displays 'show reference' dialog with passed parameters.
   *
   * @param reference reference is a fqn of file. This parameter can be {@code null} if method is
   *     called for file or folder which doesn't have fqn (e.g. ExternalLibrariesNode).
   * @param path location of node
   */
  void show(@Nullable String reference, @NotNull Path path);

  interface ActionDelegate {}
}
