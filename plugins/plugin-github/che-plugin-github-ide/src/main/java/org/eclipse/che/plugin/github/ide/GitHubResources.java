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
package org.eclipse.che.plugin.github.ide;

import com.google.gwt.resources.client.ClientBundle;
import org.eclipse.che.plugin.github.ide.importer.page.GithubImporterPageViewImpl;

/**
 * @author Ann Zhuleva
 * @version $Id: Mar 22, 2011 2:39:07 PM anya $
 */
public interface GitHubResources extends ClientBundle {

  @Source({"importer/page/GithubImporterPage.css", "org/eclipse/che/ide/api/ui/style.css"})
  GithubImporterPageViewImpl.GithubStyle githubImporterPageStyle();
}
