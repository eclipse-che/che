/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.svn.ide.commit.diff;

import com.google.common.base.Splitter;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Presenter for the {@link DiffViewerView}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class DiffViewerPresenter extends SubversionActionPresenter implements DiffViewerView.ActionDelegate {

    private static final Map<String, String> lineRules;

    private DiffViewerView view;

    static {
        lineRules = new HashMap<>();
        lineRules.put("+", "chartreuse");
        lineRules.put("-", "rgb(247, 47, 47)");
        lineRules.put("@", "cyan");
    }

    @Inject
    protected DiffViewerPresenter(AppContext appContext,
                                  SubversionOutputConsoleFactory consoleFactory,
                                  ConsolesPanelPresenter consolesPanelPresenter,
                                  ProjectExplorerPresenter projectExplorerPart,
                                  DiffViewerView view) {
        super(appContext, consoleFactory, consolesPanelPresenter, projectExplorerPart);
        this.view = view;
        this.view.setDelegate(this);
    }

    public void showDiff(String content) {
        StringBuilder html = new StringBuilder();

        html.append("<pre>");
        colorizeDiff(html, content);
        html.append("</pre>");

        view.setDiffContent(html.toString());
        view.onShow();
    }

    /** {@inheritDoc} */
    @Override
    public void onCloseClicked() {
        view.onClose();
    }

    private void colorizeDiff(StringBuilder colorized, String origin) {
        for (String line : Splitter.on("\n").splitToList(origin)) {
            final String prefix = line.substring(0, 1);
            final String sanitizedLine = new SafeHtmlBuilder().appendEscaped(line).toSafeHtml().asString();
            colorized.append("<span style=\"color:")
                     .append(lineRules.containsKey(prefix) ? lineRules.get(prefix) : "#dbdbdb")
                     .append(";\">")
                     .append(sanitizedLine)
                     .append("</span>")
                     .append("\n");
        }
    }
}
