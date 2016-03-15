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
package org.eclipse.che.ide.extension.maven.client.module;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.extension.maven.client.MavenArchetype;
import com.google.inject.ImplementedBy;

import java.util.List;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
@ImplementedBy(CreateMavenModuleViewImpl.class)
public interface CreateMavenModuleView extends View<CreateMavenModuleView.ActionDelegate> {

    MavenArchetype getArchetype();

    void setArchetypes(List<MavenArchetype> archetypes);

    void enableArchetypes(boolean enabled);

    boolean isGenerateFromArchetypeSelected();

    void setParentArtifactId(String artifactId);

    void setGroupId(String groupId);

    void setVersion(String version);

    void setCreateButtonEnabled(boolean enabled);

    void setNameError(boolean hasError);

    void setArtifactIdError(boolean hasError);

    void reset();

    String getPackaging();

    String getGroupId();

    String getVersion();

    String getArtifactId();

    String getName();

    void setPackagingVisibility(boolean visible);

    void close();

    void showButtonLoader(boolean showLoader);

    void clearArchetypes();

    public interface ActionDelegate{

        void onClose();

        void create();

        void projectNameChanged(String name);

        void artifactIdChanged(String artifactId);

        void generateFromArchetypeChanged(boolean isGenerateFromArchetype);
    }

    void show();
}
