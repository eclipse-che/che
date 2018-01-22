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
package org.eclipse.che.ide.ext.java.client.search;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.jdt.ls.extension.api.dto.UsagesResponse;

/**
 * This interface only exists to allow mocking of the view in regular unit tests.
 *
 * @author Thomas Mäder
 */
public interface FindUsagesView extends View<FindUsagesPresenter> {
  void showUsages(UsagesResponse response);
}
