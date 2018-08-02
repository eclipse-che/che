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
package org.eclipse.che.ide.projecttype.wizard;

import com.google.gwt.resources.client.ClientBundle;
import org.eclipse.che.ide.projecttype.wizard.categoriespage.CategoriesPageViewImpl;

/** @author Ann Shumilova */
public interface ProjectWizardResources extends ClientBundle {

  @Source({"categoriespage/MainPage.css", "org/eclipse/che/ide/api/ui/style.css"})
  CategoriesPageViewImpl.Style mainPageStyle();
}
