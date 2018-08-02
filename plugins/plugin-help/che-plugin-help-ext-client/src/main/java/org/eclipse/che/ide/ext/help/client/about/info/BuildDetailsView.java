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
package org.eclipse.che.ide.ext.help.client.about.info;

import org.eclipse.che.ide.api.mvp.View;

/**
 * View for the {@link BuildDetailsViewImpl}.
 *
 * @author Vlad Zhukovskyi
 * @since 6.7.0
 */
public interface BuildDetailsView extends View<BuildDetailsView.ActionDelegate> {

  void setBuildDetails(String details);

  void showDialog();

  interface ActionDelegate {}
}
