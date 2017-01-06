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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor;

import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

/**
 * This class is copy of com.codenvy.ide.core.editor.EditorInputImpl.
 *
 * @author Valeriy Svydenko
 */
public class RecipeEditorInput implements EditorInput {

    private final FileType    fileType;
    private       VirtualFile file;

    public RecipeEditorInput(@NotNull FileType fileType, @NotNull VirtualFile file) {
        this.fileType = fileType;
        this.file = file;
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getToolTipText() {
        return "";
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getName() {
        return file.getDisplayName();
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public SVGResource getSVGResource() {
        return fileType.getImage();
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public VirtualFile getFile() {
        return file;
    }

    /** {@inheritDoc} */
    @Override
    public void setFile(@NotNull VirtualFile file) {
        this.file = file;
    }

}