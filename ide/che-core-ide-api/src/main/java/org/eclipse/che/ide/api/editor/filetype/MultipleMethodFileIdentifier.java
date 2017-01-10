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
package org.eclipse.che.ide.api.editor.filetype;

import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.util.loging.Log;

import com.google.inject.Singleton;

import java.util.List;

/**
 * {@link FileTypeIdentifier} that chains multiples ways to try to recognize file types.
 *
 * @author "Mickaël Leduque"
 */
@Singleton
public class MultipleMethodFileIdentifier implements FileTypeIdentifier {

    private final FileNameFileTypeIdentifier  fileNameFileTypeIdentifier  = new FileNameFileTypeIdentifier();
    private final ExtensionFileTypeIdentifier extensionFileTypeIdentifier = new ExtensionFileTypeIdentifier();
    private final FirstLineFileTypeIdentifier firstLineFileTypeIdentifier = new FirstLineFileTypeIdentifier();

    public void registerNewExtension(String extension, List<String> contentTypes) {
        extensionFileTypeIdentifier.registerNewExtension(extension, contentTypes);
    }
    
    @Override
    public List<String> identifyType(final VirtualFile file) {
        Log.debug(MultipleMethodFileIdentifier.class, "Try identification by file name.");
        final List<String> firstTry = this.fileNameFileTypeIdentifier.identifyType(file);
        if (firstTry != null && !firstTry.isEmpty()) {
            return firstTry;
        }
        Log.debug(MultipleMethodFileIdentifier.class, "Try identification by file name suffix.");
        final List<String> secondTry = this.extensionFileTypeIdentifier.identifyType(file);
        if (secondTry != null && !secondTry.isEmpty()) {
            return secondTry;
        }
        // try harder
        Log.debug(MultipleMethodFileIdentifier.class, "Try identification by looking at the content.");
        final List<String> thirdTry = this.firstLineFileTypeIdentifier.identifyType(file);
        if (thirdTry != null && !thirdTry.isEmpty()) {
            return thirdTry;
        }
        // other means may be added later
        Log.debug(MultipleMethodFileIdentifier.class, "No identification method gave an answer.");
        return null;
    }

}
