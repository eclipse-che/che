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
package org.eclipse.che.ide.api.resources.modification;

import com.google.common.annotations.Beta;
import org.eclipse.che.ide.api.app.AppContext;

/**
 * Provides ability to paste resources which is in clipboard now.
 *
 * <p>Provider checks whether elements in application context can be paste and if so, then paste the
 * last ones.
 *
 * <p>Note, that this interface is not intended to be implemented by third-party components.
 *
 * <p>Examples of usage:
 *
 * <pre>
 *     CopyPasteSupport copySupport = ... ;
 *     AppContext appContext = ... ;
 *
 *     if (copySupport.getPasteProvider().isPastePossible(appContext)) {
 *         copySupport.getPasteProvider().performPaste(appContext);
 *     }
 * </pre>
 *
 * @author Vlad Zhukovskiy
 * @see ClipboardManager
 * @see ClipboardManager#getPasteProvider() ()
 * @since 4.4.0
 */
@Beta
public interface PasteProvider {
  /**
   * Checks whether resources which is in clipboard context, can be paste.
   *
   * @param appContext the application context
   * @return {@code true} if active resources can be paste, otherwise {@code false}
   * @since 4.4.0
   */
  boolean isPastePossible(AppContext appContext);

  /**
   * Performs pasting resources from the given clipboard context.
   *
   * @param appContext the application context
   * @since 4.4.0
   */
  void performPaste(AppContext appContext);
}
