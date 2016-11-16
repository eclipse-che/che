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
package org.eclipse.che.ide.search;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

/**
 * View for text search (find file with occurrences).
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(FullTextSearchViewImpl.class)
public interface FullTextSearchView extends View<FullTextSearchView.ActionDelegate> {
    /** Needs for delegate some function into NavigateToFile view. */
    interface ActionDelegate {
        /** Called when Search button clicked. */
        void search(String text);

        /** Seth path of directory to search */
        void setPathDirectory(String path);

        /** Set focus to root window. */
        void setFocus();

        /** Called when Enter clicked */
        void onEnterClicked();
    }

    /** Shows error message on the view */
    void showErrorMessage(String message);

    /** Clear input. */
    void clearInput();

    /** Set focus to root window. */
    void setFocus();

    /** Returns {@code true} if accept button is in the focus and {@code false} - otherwise. */
    boolean isAcceptButtonInFocus();

    /** Returns {@code true} if Cancel button is in the focus and {@code false} - otherwise. */
    boolean isCancelButtonInFocus();

    /** Returns {@code true} if 'select path' button is in the focus and {@code false} - otherwise. */
    boolean isSelectPathButtonInFocus();

    void showSelectPathDialog();

    /** Close dialog. */
    void close();

    /** Show dialog. */
    void showDialog();

    /** Seth path of directory to search */
    void setPathDirectory(String path);

    /** Returns text for searching */
    String getSearchText();

    /** Returns file mask for searching */
    String getFileMask();

    /** Returns path to start search */
    String getPathToSearch();

    /**
     * True if need find only whole word
     * @return
     */
    boolean isWholeWordsOnly();
}
