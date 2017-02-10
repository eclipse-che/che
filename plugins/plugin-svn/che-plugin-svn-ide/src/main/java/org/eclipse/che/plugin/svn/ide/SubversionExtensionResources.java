/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.svn.ide;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import org.eclipse.che.plugin.svn.ide.importer.SubversionProjectImporterViewImpl;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Interface providing access to client resources.
 *
 * @author Jeremy Whitlock
 */
public interface SubversionExtensionResources extends ClientBundle {

    /**
     * CSS for Subversion.
     */
    interface SubversionCSS extends CssResource {
        String textFont();
    }

    @Source({"importer/SubversionProjectImporterView.css", "org/eclipse/che/ide/api/ui/style.css"})
    SubversionProjectImporterViewImpl.Style svnProjectImporterPageStyle();

    @Source({"subversion.css", "org/eclipse/che/ide/api/ui/style.css"})
    SubversionCSS subversionCSS();

    @Source("actions/add.svg")
    SVGResource add();

    @Source("actions/apply-patch.svg")
    SVGResource applyPatch();

    @Source("actions/branch-tag.svg")
    SVGResource branchTag();

    @Source("actions/commit.svg")
    SVGResource commit();

    @Source("actions/copy.svg")
    SVGResource copy();

    @Source("actions/move.svg")
    SVGResource move();

    @Source("actions/alert.svg")
    SVGResource alert();

    @Source("actions/cleanup.svg")
    SVGResource cleanup();

    @Source("actions/create-patch.svg")
    SVGResource createPatch();

    @Source("actions/delete.svg")
    SVGResource delete();

    @Source("actions/diff.svg")
    SVGResource diff();

    @Source("actions/export.svg")
    SVGResource export();

    @Source("actions/lock.svg")
    SVGResource lock();

    @Source("actions/log.svg")
    SVGResource log();

    @Source("actions/merge.svg")
    SVGResource merge();

    @Source("actions/properties.svg")
    SVGResource properties();

    @Source("actions/relocate.svg")
    SVGResource relocate();

    @Source("actions/rename.svg")
    SVGResource rename();

    @Source("actions/resolved.svg")
    SVGResource resolved();

    @Source("actions/revert.svg")
    SVGResource revert();

    @Source("actions/status.svg")
    SVGResource status();

    @Source("actions/switch.svg")
    SVGResource switchLocation();

    @Source("actions/unlock.svg")
    SVGResource unlock();

    @Source("actions/update.svg")
    SVGResource update();

    @Source("actions/svn.svg")
    SVGResource svn();

    @Source("output-icon.svg")
    SVGResource outputIcon();
}
