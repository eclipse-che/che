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
package org.eclipse.che.ide.core.editor;

import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.extension.SDK;
import org.eclipse.che.ide.api.filetypes.FileType;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Registry for holding {@link EditorProvider} for specific {@link FileType}.
 *
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 */
@SDK(title = "ide.api.editorRegistry")
public class EditorRegistryImpl implements EditorRegistry {

    private Map<String, List<EditorProvider>> registry;
    private EditorProvider                    defaultProvider;

    @Inject
    public EditorRegistryImpl(@Named("defaultEditor") EditorProvider defaultProvider,
                              @Named("defaultFileType") FileType defaultFile) {
        super();
        this.defaultProvider = defaultProvider;
        registry = new HashMap<>();
        register(defaultFile, defaultProvider);
    }

    /** {@inheritDoc} */
    @Override
    public void register(@NotNull FileType fileType, @NotNull EditorProvider provider) {
        if (!registry.containsKey(fileType.getId())) {
            registry.put(fileType.getId(), new ArrayList<EditorProvider>());
        }
        registry.get(fileType.getId()).add(provider);
    }

    @Override
    public void registerDefaultEditor(@NotNull FileType fileType, @NotNull EditorProvider provider) {
        //todo store default editor, add checks to ensure that default editor sets only one time
        register(fileType, provider);
    }

    /** {@inheritDoc} */
    @Override
    public EditorProvider getEditor(@NotNull FileType fileType) {
        //todo add logic to receive default editor form user preferences
        if (registry.containsKey(fileType.getId()) && !registry.get(fileType.getId()).isEmpty()) {
            return registry.get(fileType.getId()).get(0);
        }
        return defaultProvider;
    }

    @Override
    public List<EditorProvider> getAllEditorsForFileType(@NotNull FileType fileType) {
        List<EditorProvider> result = new ArrayList<>();
        if (registry.containsKey(fileType.getId())) {
            result.addAll(registry.get(fileType.getId()));
        }
        return result;
    }
}
