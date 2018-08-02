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
package org.eclipse.che.ide.preferences.pages.extensions;

import java.util.List;
import org.eclipse.che.ide.api.extension.ExtensionDescription;
import org.eclipse.che.ide.api.mvp.View;

/** @author Evgen Vidolob */
public interface ExtensionManagerView extends View<ExtensionManagerView.ActionDelegate> {

  void setExtensions(List<ExtensionDescription> extensions);

  interface ActionDelegate {}
}
