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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.model.workspace.ProjectConfig;
import org.eclipse.che.api.vfs.shared.dto.AccessControlEntry;
import org.eclipse.che.api.vfs.shared.dto.Principal;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Vitaly Parfonov
 */
public class DtoConverterTest {

    /**
     * Check user permission is based on userID and not username.
     *
     * @throws Exception
     *         if something is going wrong
     */
    @Test
    public void toProjectsDescriptorUserPermissionID() throws Exception {

        String userId = "florentID";
        String userName = "florent";

        List<String> permissions = Arrays.asList("read", "write");

        // setup project
        Project project = mock(Project.class);

        // project permissions
        AccessControlEntry accessControlEntry = mock(AccessControlEntry.class);
        List<AccessControlEntry> acl = Arrays.asList(accessControlEntry);
        doReturn(acl).when(project).getPermissions();
        ProjectConfig projectConfig = mock(ProjectConfig.class);
        when(project.getConfig()).thenReturn(projectConfig);
        when(projectConfig.getSource()).thenReturn(mock(SourceStorage.class));
        Principal principal = mock(Principal.class);
        doReturn(principal).when(accessControlEntry).getPrincipal();
        doReturn(permissions).when(accessControlEntry).getPermissions();

        // VFS permissions are set on user id
        doReturn(Principal.Type.USER).when(principal).getType();
        doReturn(userId).when(principal).getName();


        // environment context
        EnvironmentContext customEnvironment = mock(EnvironmentContext.class);
        User user = mock(User.class);
        doReturn(user).when(customEnvironment).getUser();
        doReturn(userId).when(user).getId();
        doReturn(userName).when(user).getName();

        // launch convert and before set env context
        EnvironmentContext old = EnvironmentContext.getCurrent();
        ProjectConfigDto projectConfigDto;
        try {
            EnvironmentContext.setCurrent(customEnvironment);
            projectConfigDto = DtoConverter.toProjectConfig(project, null);
        } finally {
            // reset
            EnvironmentContext.setCurrent(old);
        }

        Assert.assertNotNull(projectConfigDto);
    }
}
