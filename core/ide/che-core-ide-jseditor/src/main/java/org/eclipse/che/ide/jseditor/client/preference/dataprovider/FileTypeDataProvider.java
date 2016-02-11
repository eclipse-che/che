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
package org.eclipse.che.ide.jseditor.client.preference.dataprovider;

import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An {@link AbstractDataProvider} implementation for {@link FileType} based on the {@link FileTypeRegistry}.
 *
 * @author "Mickaël Leduque"
 */
public class FileTypeDataProvider extends AbstractDataProvider<FileType> implements RefreshableDataProvider {

    /** The file type registry. */
    private final FileTypeRegistry fileTypeRegistry;

    @Inject
    public FileTypeDataProvider(final FileTypeRegistry fileTypeRegistry) {
        super(new FileTypeKeyProvider());
        this.fileTypeRegistry = fileTypeRegistry;
    }

    @Override
    protected void onRangeChanged(final HasData<FileType> display) {
        final List<FileType> list = buildData();
        if (!list.isEmpty()) {
            updateRowData(display, 0, list);
        }
    }

    public void refresh() {
        final List<FileType> list = buildData();
        this.updateRowData(0, list);
        this.updateRowCount(list.size(), true);
    }

    private List<FileType> buildData() {
        final List<FileType> filetypes = this.fileTypeRegistry.getRegisteredFileTypes();
        int size = filetypes.size();
        if (size > 0) {
            final List<FileType> list = new ArrayList<>(size);
            for (final FileType filetype : filetypes) {
                list.add(filetype);
            }
            return list;
        } else {
            return Collections.emptyList();
        }
    }
}
