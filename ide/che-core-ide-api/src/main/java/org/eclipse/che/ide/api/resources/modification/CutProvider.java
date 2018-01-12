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
package org.eclipse.che.ide.api.resources.modification;

import com.google.common.annotations.Beta;
import org.eclipse.che.ide.api.app.AppContext;

/**
 * Provides ability to cut resources which is in given context.
 *
 * <p>Provider checks whether elements in application context can be cut and if so, then cutting the
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
 *     if (copySupport.getCutProvider().isCutEnable(appContext)) {
 *         copySupport.getCutProvider().performCut(appContext);
 *     }
 * </pre>
 *
 * @author Vlad Zhukovskiy
 * @see ClipboardManager
 * @see ClipboardManager#getCutProvider()
 * @since 4.4.0
 */
@Beta
public interface CutProvider {
  /**
   * Checks whether resources which is in context, can be cut.
   *
   * @param appContext the application context
   * @return {@code true} if active resources can be cut, otherwise {@code false}
   * @since 4.4.0
   */
  boolean isCutEnable(AppContext appContext);

  /**
   * Performs cutting resources from the given application context.
   *
   * @param appContext the application context
   * @since 4.4.0
   */
  void performCut(AppContext appContext);
}
