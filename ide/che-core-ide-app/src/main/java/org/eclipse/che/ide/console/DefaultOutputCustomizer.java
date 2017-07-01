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
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.text.TextPosition;
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

    private static final RegExp LINE_AT = compile("(\\s+at .+\\(.+\\.java:\\d+\\))");

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
        return (LINE_AT.exec(text) != null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.che.ide.extension.machine.client.outputspanel.console.
     * OutputCustomizer#customize(java.lang.String)
     */
    @Override
    public String customize(String text) {
        MatchResult matcher = LINE_AT.exec(text);
        if (matcher != null) {
            try {
                int start = text.indexOf("at", 0) + "at".length(), openBracket = text.indexOf("(", start),
                        column = text.indexOf(":", openBracket), closingBracket = text.indexOf(")", column);
                String qualifiedName = text.substring(start, openBracket).trim();
                String fileName = text.substring(openBracket + "(".length(), column).trim();
                int lineNumber = Integer.valueOf(text.substring(column + ":".length(), closingBracket).trim());

                String customText = text.substring(0, openBracket + "(".length());
                customText += "<a href='javascript:open(\"" + qualifiedName + "\", \"" + fileName + "\", " + lineNumber
                        + ");'>";
                customText += text.substring(openBracket + "(".length(), closingBracket);
                customText += "</a>";
                customText += text.substring(closingBracket);
                text = customText;
            } catch (IndexOutOfBoundsException ex) {
                // ignore
            } catch (NumberFormatException ex) {
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

        String relativeFilePath = (packageName.isEmpty() ? "" : (packageName.replace(".", "/") + "/")) + fileName;

        collectChildren(appContext.getWorkspaceRoot(), Path.valueOf(relativeFilePath))
        .then(files -> {
            if (!files.isEmpty()) {
                openFileInEditorAndReveal(appContext, editorAgent, files.get(0).getLocation(), lineNumber, 0);
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
                return Stream.of(children)
                        .filter(child -> child.isFile() && endsWith(child.asFile().getLocation(), relativeFilePath))
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

    /**
     * Finds a file by its path, opens it in editor and sets the text selection and
     * reveals according to the specified line and column numbers
     * 
     * @param file
     * @param lineNumber
     * @param columnNumber
     */
    static void openFileInEditorAndReveal(AppContext appContext, EditorAgent editorAgent, Path file,
            final int lineNumber, final int columnNumber) {
        appContext.getWorkspaceRoot().getFile(file).then(optional -> {
            if (optional.isPresent()) {
                editorAgent.openEditor(optional.get(), new OpenEditorCallbackImpl() {
                    @Override
                    public void onEditorOpened(EditorPartPresenter editor) {
                        Timer t = new Timer() {
                            @Override
                            public void run() {
                                EditorPartPresenter editorPart = editorAgent.getActiveEditor();
                                selectRange(editorPart, lineNumber, columnNumber);
                            }
                        };
                        t.schedule(500);
                    }

                    @Override
                    public void onEditorActivated(EditorPartPresenter editor) {
                        selectRange(editor, lineNumber, columnNumber);
                    }
                });
            }
        });
    }

    /**
     * Selects and shows the specified line and column of text in editor
     * 
     * @param editor
     * @param line
     * @param column
     */
    static void selectRange(EditorPartPresenter editor, int line, int column) {
        line--;
        column--;
        if (line < 0)
            line = 0;
        if (editor instanceof TextEditor) {
            Document document = ((TextEditor) editor).getDocument();
            LinearRange selectionRange = document.getLinearRangeForLine(line);
            if (column >= 0) {
                selectionRange = LinearRange.createWithStart(selectionRange.getStartOffset() + column).andLength(0);
            }
            document.setSelectedRange(selectionRange, true);
            document.setCursorPosition(new TextPosition(line, column >= 0 ? column : 0));
        }
    }

    /*
     * Selects and shows the specified line and column of text in editor
     */
    private void selectRange(EditorPartPresenter editor, int line) {
        line--;
        if (editor instanceof TextEditor) {
            Document document = ((TextEditor) editor).getDocument();
            LinearRange selectionRange = document.getLinearRangeForLine(line);
            document.setCursorPosition(new TextPosition(line, 0));
            document.setSelectedRange(selectionRange, true);
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
