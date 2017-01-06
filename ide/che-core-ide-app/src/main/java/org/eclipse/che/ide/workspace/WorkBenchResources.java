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
package org.eclipse.che.ide.workspace;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
public interface WorkBenchResources extends ClientBundle {
    interface WorkBenchCss extends CssResource {

        @ClassName("ide-work-bench-tool-panel-bottom")
        String ideWorkBenchToolPanelBottom();

        @ClassName("ide-work-bench-tool-panel-left")
        String ideWorkBenchToolPanelLeft();

        @ClassName("ide-work-bench-tool-panel-right")
        String ideWorkBenchToolPanelRight();

        @ClassName("ide-work-bench-split-panel-left")
        String ideWorkBenchSplitPanelLeft();

        @ClassName("ide-work-bench-split-panel-right")
        String ideWorkBenchSplitPanelRight();

        @ClassName("ide-work-bench-split-panel-bottom")
        String ideWorkBenchSplitPanelBottom();

        @ClassName("ide-work-bench-parent-panel")
        String ideWorkBenchParentPanel();
    }

    @Source({"perspectives/general/WorkBench.css", "org/eclipse/che/ide/api/ui/style.css"})
    WorkBenchCss workBenchCss();
}
