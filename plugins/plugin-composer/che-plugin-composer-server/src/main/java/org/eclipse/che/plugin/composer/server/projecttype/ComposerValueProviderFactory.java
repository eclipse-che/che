/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.composer.server.projecttype;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FileEntry;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.type.ReadonlyValueProvider;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.project.server.type.ValueStorageException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.plugin.composer.shared.Constants.PACKAGE;

public class ComposerValueProviderFactory implements ValueProviderFactory {

    @Override
    public ValueProvider newInstance(FolderEntry projectFolder) {
        return new ComposerValueProvider(projectFolder);
    }

    protected class ComposerValueProvider extends ReadonlyValueProvider {

        protected FolderEntry projectFolder;

        protected ComposerValueProvider(FolderEntry projectFolder) {
            this.projectFolder = projectFolder;
        }

        @Override
        public List<String> getValues(String attributeName) throws ValueStorageException {
            try {
                if (projectFolder.getChild("composer.json") == null) {
                    return Collections.emptyList();
                }
                JsonObject model = readModel(projectFolder);
                String value = "";

                if (attributeName.equals(PACKAGE)) {
                    value = model.get("name").getAsString();
                }

                return Collections.singletonList(value);
            } catch (ServerException | IOException e) {
                throw new ValueStorageException("Can't read composer.json : " + e.getMessage());
            }
        }

        private JsonObject readModel(FolderEntry projectFolder) throws ServerException, IOException {
            FileEntry composerFile = (FileEntry) projectFolder.getChild("composer.json");
            Reader reader = new BufferedReader(new InputStreamReader(composerFile.getInputStream()));
            return new Gson().fromJson(reader, JsonObject.class);
        }
    }

}
