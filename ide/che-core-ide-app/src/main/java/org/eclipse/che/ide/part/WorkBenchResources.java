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
package org.eclipse.che.ide.part;

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
