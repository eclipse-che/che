/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import org.eclipse.che.ide.ext.git.client.importer.page.GitImporterPageViewImpl;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 */
public interface GitResources extends ClientBundle {

  interface GitCSS extends CssResource {
    String textFont();

    String cells();

    String simpleListContainer();

    String emptyBorder();

    String spacing();
  }

  interface GitPanelCss extends CssResource {
    String fullHeight();

    String topIndent();

    String bottomIndent();

    String verticalItems();

    String repositoryChangesLabel();
  }

  interface Css extends CssResource {
    String insertion();

    String modification();

    String deletion();

    @ClassName("git-change-marker")
    String marker();
  }

  @Source({"importer/page/GitImporterPage.css", "org/eclipse/che/ide/api/ui/style.css"})
  GitImporterPageViewImpl.Style gitImporterPageStyle();

  @Source({"git.css", "org/eclipse/che/ide/api/ui/style.css"})
  GitCSS gitCSS();

  @Source("panel/gitPanel.css")
  GitPanelCss gitPanelCss();

  @Source({"changeMarkers.css", "org/eclipse/che/ide/api/ui/style.css"})
  Css changeMarkersCSS();

  @Source("panel/git.svg")
  SVGResource git();

  @Source("panel/git-logo.svg")
  SVGResource gitLogo();

  @Source("panel/repository.svg")
  SVGResource repository();

  @Source("push/arrow.svg")
  SVGResource arrow();

  @Source("controls/init.svg")
  SVGResource initRepo();

  @Source("controls/delete-repo.svg")
  SVGResource deleteRepo();

  @Source("controls/merge.svg")
  SVGResource merge();

  @Source("controls/branches.svg")
  SVGResource branches();

  @Source("controls/remotes.svg")
  SVGResource remotes();

  @Source("controls/commit.svg")
  SVGResource commit();

  @Source("controls/push.svg")
  SVGResource push();

  @Source("controls/pull.svg")
  SVGResource pull();

  @Source("controls/checkoutReference.svg")
  SVGResource checkoutReference();

  @Source("history/history.svg")
  SVGResource history();

  @Source("history/project_level.svg")
  SVGResource projectLevel();

  @Source("history/resource_level.svg")
  SVGResource resourceLevel();

  @Source("history/diff_index.svg")
  SVGResource diffIndex();

  @Source("history/diff_working_dir.svg")
  SVGResource diffWorkTree();

  @Source("history/diff_prev_version.svg")
  SVGResource diffPrevVersion();

  @Source("history/refresh.svg")
  SVGResource refresh();

  @Source("controls/fetch.svg")
  SVGResource fetch();

  @Source("branch/current.svg")
  SVGResource currentBranch();

  @Source("controls/remote.svg")
  SVGResource remote();

  @Source("controls/git-output-icon.svg")
  SVGResource gitOutput();

  @Source("controls/revert.svg")
  SVGResource revert();

  @Source("icons/added.svg")
  SVGResource iconAdded();

  @Source("icons/modified.svg")
  SVGResource iconModified();

  @Source("icons/deleted.svg")
  SVGResource iconDeleted();

  @Source("icons/renamed.svg")
  SVGResource iconRenamed();

  @Source("icons/copied.svg")
  SVGResource iconCopied();

  @Source("icons/untracked.svg")
  SVGResource iconUntracked();
}
