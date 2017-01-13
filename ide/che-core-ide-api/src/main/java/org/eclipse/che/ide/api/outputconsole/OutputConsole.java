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
package org.eclipse.che.ide.api.outputconsole;

import org.eclipse.che.ide.api.mvp.Presenter;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Describes requirements for the console for displaying output. 
 *
 * @author Roman Nikitenko
 */
public interface OutputConsole extends Presenter {

    /** Return title for the console. */
    String getTitle();

    /**
     * Returns the title SVG image resource of this console.
     *
     * @return the title SVG image resource
     */
    SVGResource getTitleIcon();

    /** Checks whether the console is finished outputting or not. */
    boolean isFinished();

    /** Stop process. */
    void stop();

    /** Called when console is closed. */
    void close();

    /**
     * Action Delegate interface.
     */
    interface ActionDelegate {

        /** Is called when new is printed */
        void onConsoleOutput(OutputConsole console);

        /** Is called when user asked to download output */
        void onDownloadOutput(OutputConsole console);

    }

    /**
     * Sets action delegate.
     */
    void addActionDelegate(ActionDelegate actionDelegate);

}
