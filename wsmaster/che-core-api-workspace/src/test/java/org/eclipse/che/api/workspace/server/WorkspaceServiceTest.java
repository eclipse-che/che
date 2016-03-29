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
package org.eclipse.che.api.workspace.server;

import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.permission.PermissionManager;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.UserImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.RequestFilter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link WorkspaceService}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class WorkspaceServiceTest {

//    @Filter
//    public static class EnvironmentFilter implements RequestFilter {
//
//        public void doFilter(GenericContainerRequest request) {
//        }
//    }

    @SuppressWarnings("unused")
    private static final ApiExceptionMapper MAPPER  = new ApiExceptionMapper();
    private static final String             USER_ID = "user123";
    private static final LinkedList<String> ROLES   = new LinkedList<>(singleton("user"));
    @SuppressWarnings("unused")
    private static final RequestFilter      FILTER  = request -> {
        EnvironmentContext.getCurrent().setUser(new UserImpl("user", USER_ID, "token", ROLES, false));
    };

    @Mock
    private WorkspaceManager   manager;
    @Mock
    private MachineManager     machineManager;
    @Mock
    private WorkspaceValidator validator;
    @Mock
    private PermissionManager  permissionManager;
    @InjectMocks
    private WorkspaceService   service;

    @Test
    public void test() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(createConfigDto())
                                         .when()
                                         .post(SECURE_PATH + "/workspace");

        assertEquals(response.getStatusCode(), 200);
    }

    private static <T> T unwrapDto(Response response, Class<T> dtoClass) {
        return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
    }

    private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass) {
        return DtoFactory.getInstance().createListDtoFromJson(response.body().print(), dtoClass)
                         .stream()
                         .collect(toList());
    }

    private static WorkspaceConfigDto createConfigDto() {
        final MachineConfigImpl devMachine = MachineConfigImpl.builder()
                                                              .setDev(true)
                                                              .setName("dev-machine")
                                                              .setType("docker")
                                                              .setSource(new MachineSourceImpl("location", "recipe"))
                                                              .setServers(asList(new ServerConfImpl("ref1",
                                                                                                    "8080",
                                                                                                    "https",
                                                                                                    "path1"),
                                                                                 new ServerConfImpl("ref2",
                                                                                                    "8081",
                                                                                                    "https",
                                                                                                    "path2")))
                                                              .setEnvVariables(singletonMap("key1", "value1"))
                                                              .build();
        final WorkspaceConfigImpl config = WorkspaceConfigImpl.builder()
                                                              .setName("dev-workspace")
                                                              .setDefaultEnv("dev-env")
                                                              .setEnvironments(singletonList(
                                                                      new EnvironmentImpl("dev-env", null, singletonList(devMachine))))
                                                              .build();
        return DtoConverter.asDto(config);
    }
}