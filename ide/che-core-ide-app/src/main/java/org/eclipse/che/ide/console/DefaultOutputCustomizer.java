/*******************************************************************************
 * Copyright (c) 2017 RedHat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   RedHat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.console;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.gwt.regexp.shared.RegExp.compile;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resource.Path;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;

/**
 * Default customizer adds an anchor link to the lines that match a stack trace
 * line pattern and installs a handler function for the link. The handler parses
 * the stack trace line, searches for the candidate Java files to navigate to,
 * opens the first file (of the found candidates) in editor and reveals it to
 * the required line according to the stack trace line information
 */
public class DefaultOutputCustomizer implements OutputCustomizer {

    private static final RegExp LINE_AT = compile("(\\s+at .+)");
    private static final RegExp LINE_AT_EXCEPTION = compile("(\\s+at address:.+)");

    private AppContext appContext;
    private EditorAgent editorAgent;

    @Inject
    public DefaultOutputCustomizer(AppContext appContext, EditorAgent editorAgent) {
        this.appContext = appContext;
        this.editorAgent = editorAgent;

        exportAnchorClickHandlerFunction();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.che.ide.extension.machine.client.outputspanel.console.
     * OutputCustomizer#canCustomize(java.lang.String)
     */
    @Override
    public boolean canCustomize(String text) {
        return (LINE_AT.exec(text) != null && LINE_AT_EXCEPTION.exec(text) == null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.che.ide.extension.machine.client.outputspanel.console.
     * OutputCustomizer#customize(java.lang.String)
     */
    @Override
    public String customize(String text) {
        String customText = text;

        MatchResult matcher = LINE_AT.exec(text);
        if (matcher != null) {
            try {
                int start = text.indexOf("at", 0) + "at".length(), openBracket = text.indexOf("(", start),
                        column = text.indexOf(":", openBracket), closingBracket = text.indexOf(")", column);
                String qualifiedName = text.substring(start, openBracket).trim();
                String fileName = text.substring(openBracket + "(".length(), column).trim();
                int lineNumber = Integer.valueOf(text.substring(column + ":".length(), closingBracket).trim());
                customText = text.substring(0, openBracket + "(".length());
                customText += "<a href='javascript:open(\"" + qualifiedName + "\", \"" + fileName + "\", " + lineNumber
                        + ");'>";
                customText += text.substring(openBracket + "(".length(), closingBracket);
                customText += "</a>";
                customText += text.substring(closingBracket);
                text = customText;
            } catch (IndexOutOfBoundsException ex) {
                // ignore
            }
        }

        return text;
    }

    /**
     * A callback that is to be called for an anchor
     * 
     * @param qualifiedName
     * @param fileName
     * @param lineNumber
     */
    public void handleAnchorClick(String qualifiedName, String fileName, final int lineNumber) {
        if (qualifiedName == null || fileName == null) {
            return;
        }

        String qualifiedClassName = qualifiedName.lastIndexOf('.') != -1
                ? qualifiedName.substring(0, qualifiedName.lastIndexOf('.'))
                : qualifiedName;
        final String packageName = qualifiedClassName.lastIndexOf('.') != -1
                ? qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'))
                : "";

        String relativeFilePath = (packageName.isEmpty() ? "" : 
            (packageName.replace(".", "/") + "/")) + fileName;

        collectChildren(appContext.getWorkspaceRoot(), Path.valueOf(relativeFilePath)).then(files -> {
            if (!files.isEmpty()) {
                editorAgent.openEditor(files.get(0), new OpenEditorCallbackImpl() {
                    @Override
                    public void onEditorOpened(EditorPartPresenter editor) {
                        Timer t = new Timer() {
                            @Override
                            public void run() {
                                EditorPartPresenter editorPart = editorAgent.getActiveEditor();
                                selectRange(editorPart, lineNumber);
                            }
                        };
                        t.schedule(500);
                    }

                    @Override
                    public void onEditorActivated(EditorPartPresenter editor) {
                        selectRange(editor, lineNumber);
                    }
                });

            }
        });
    }

    /*
     * Returns the list of workspace files filtered by a relative path
     */
    private Promise<List<File>> collectChildren(Container root, Path relativeFilePath) {
        return root.getTree(-1).then(new Function<Resource[], List<File>>() {
            @Override
            public List<File> apply(Resource[] children) throws FunctionException {
                return Stream.of(children).filter(
                        child -> child.isFile() && endsWith(child.asFile().getLocation(), relativeFilePath))
                        .map(Resource::asFile).collect(Collectors.toList());
            }
        });
    }

    /*
     * Checks if a path's last segments are equal to the provided relative path
     */
    private boolean endsWith(Path path, Path relativePath) {
        checkNotNull(path);
        checkNotNull(relativePath);

        if (path.segmentCount() < relativePath.segmentCount())
            return false;

        for (int i = relativePath.segmentCount() - 1, j = path.segmentCount() - 1; i >= 0; i--, j--) {
            if (!nullToEmpty(relativePath.segment(i)).equals(path.segment(j))) {
                return false;
            }
        }

        return true;
    }

    /*
     * Selects and shows the specified line of text in editor
     */
    private void selectRange(EditorPartPresenter editor, int line) {
        if (editor instanceof TextEditor) {
            TextPosition startPosition = new TextPosition(line - 1, 0);
            int lineOffsetStart = ((TextEditor) editor).getDocument().getLineStart(line - 1);
            if (lineOffsetStart == -1) {
                lineOffsetStart = 0;
            }

            int lineOffsetEnd = ((TextEditor) editor).getDocument().getLineStart(line);
            if (lineOffsetEnd == -1) {
                lineOffsetEnd = 0;
            }
            while (((TextEditor) editor).getDocument().getLineAtOffset(lineOffsetEnd) > line - 1) {
                lineOffsetEnd--;
            }
            if (lineOffsetStart > lineOffsetEnd) {
                lineOffsetEnd = lineOffsetStart;
            }

            TextPosition endPosition = new TextPosition(line - 1, lineOffsetEnd - lineOffsetStart);

            ((TextEditor) editor).getDocument().setSelectedRange(new TextRange(startPosition, endPosition), true);
            ((TextEditor) editor).getDocument().setCursorPosition(startPosition);
        }
    }

    /**
     * Sets up a java callback to be called for an anchor
     */
    public native void exportAnchorClickHandlerFunction() /*-{
        var that = this;
        $wnd.open = $entry(function(qualifiedName,fileName,lineNumber) {
        that.@org.eclipse.che.ide.console.DefaultOutputCustomizer::handleAnchorClick(*)(qualifiedName,fileName,lineNumber);
        });
    }-*/;
}
