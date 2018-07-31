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
package org.eclipse.che.ide.ext.java.client.search;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesResponse;

/**
 * View for find usages result
 *
 * @author Evgen Vidolob
 */
@ImplementedBy(FindUsagesViewImpl.class)
public interface FindUsagesView extends View<FindUsagesView.ActionDelegate> {

  void setVisible(boolean visible);

  void showUsages(FindUsagesResponse usagesResponse);

  interface ActionDelegate extends BaseActionDelegate {}
}
