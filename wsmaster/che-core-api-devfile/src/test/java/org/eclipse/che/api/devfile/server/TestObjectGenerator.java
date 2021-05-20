/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.devfile.server;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.devfile.shared.dto.UserDevfileDto;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ActionImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EntrypointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EnvImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.MetadataImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ProjectImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.SourceImpl;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;

public class TestObjectGenerator {

  public static final String TEST_CHE_NAMESPACE = "user";
  public static final String CURRENT_USER_ID = NameGenerator.generate("usrid", 6);
  public static final Subject TEST_SUBJECT =
      new SubjectImpl(TEST_CHE_NAMESPACE, CURRENT_USER_ID, "token", false);
  public static final String USER_DEVFILE_ID = NameGenerator.generate("usrd", 16);
  public static final AccountImpl TEST_ACCOUNT =
      new AccountImpl("acc-id042u3ui3oi", TEST_CHE_NAMESPACE, "test");

  public static UserDevfileDto createUserDevfileDto() {
    return DtoConverter.asDto(createUserDevfile(NameGenerator.generate("name", 6)));
  }

  public static UserDevfileImpl createUserDevfile() {
    return createUserDevfile(NameGenerator.generate("name", 6));
  }

  public static UserDevfileImpl createUserDevfile(String name) {
    return createUserDevfile(NameGenerator.generate("id", 6), name);
  }

  public static UserDevfileImpl createUserDevfile(String id, String name) {
    return new UserDevfileImpl(id, TEST_ACCOUNT, name, "devfile description", createDevfile(name));
  }

  public static UserDevfileImpl createUserDevfile(String id, Account account, String name) {
    return new UserDevfileImpl(id, account, name, "devfile description", createDevfile(name));
  }

  public static UserDevfileImpl createUserDevfile(Account account) {
    return createUserDevfile(
        NameGenerator.generate("id", 6), account, NameGenerator.generate("name", 6));
  }

  public static DevfileImpl createDevfile(String generatedName) {
    return createDevfile(null, generatedName);
  }

  public static DevfileImpl createDevfileWithName(String name) {
    return createDevfile(name, null);
  }

  private static DevfileImpl createDevfile(String name, String generatedName) {
    String effectiveName = MoreObjects.firstNonNull(name, generatedName);
    SourceImpl source1 =
        new SourceImpl(
            "type1",
            "http://location",
            "branch1",
            "point1",
            "tag1",
            "commit1",
            "sparseCheckoutDir1");
    ProjectImpl project1 = new ProjectImpl("project1", source1, "path1");

    SourceImpl source2 =
        new SourceImpl(
            "type2",
            "http://location",
            "branch2",
            "point2",
            "tag2",
            "commit2",
            "sparseCheckoutDir2");
    ProjectImpl project2 = new ProjectImpl("project2", source2, "path2");

    ActionImpl action1 =
        new ActionImpl("exec1", "component1", "run.sh", "/home/user/1", null, null);
    ActionImpl action2 =
        new ActionImpl("exec2", "component2", "run.sh", "/home/user/2", null, null);

    CommandImpl command1 =
        new CommandImpl(
            effectiveName + "-1", singletonList(action1), singletonMap("attr1", "value1"), null);
    CommandImpl command2 =
        new CommandImpl(
            effectiveName + "-2", singletonList(action2), singletonMap("attr2", "value2"), null);

    EntrypointImpl entrypoint1 =
        new EntrypointImpl(
            "parentName1",
            singletonMap("parent1", "selector1"),
            "containerName1",
            asList("command1", "command2"),
            asList("arg1", "arg2"));

    EntrypointImpl entrypoint2 =
        new EntrypointImpl(
            "parentName2",
            singletonMap("parent2", "selector2"),
            "containerName2",
            asList("command3", "command4"),
            asList("arg3", "arg4"));

    org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl volume1 =
        new org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl("name1", "path1");

    org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl volume2 =
        new org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl("name2", "path2");

    EnvImpl env1 = new EnvImpl("name1", "value1");
    EnvImpl env2 = new EnvImpl("name2", "value2");

    EndpointImpl endpoint1 = new EndpointImpl("name1", 1111, singletonMap("key1", "value1"));
    EndpointImpl endpoint2 = new EndpointImpl("name2", 2222, singletonMap("key2", "value2"));

    ComponentImpl component1 =
        new ComponentImpl(
            "kubernetes",
            "component1",
            null,
            null,
            null,
            null,
            "refcontent1",
            ImmutableMap.of("app.kubernetes.io/component", "db"),
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            false,
            null,
            null,
            null,
            asList(env1, env2),
            null);
    component1.setSelector(singletonMap("key1", "value1"));

    ComponentImpl component2 =
        new ComponentImpl(
            "dockerimage",
            "component2",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "image",
            "256G",
            null,
            "3",
            "180m",
            false,
            false,
            singletonList("command"),
            singletonList("arg"),
            asList(volume1, volume2),
            asList(env1, env2),
            asList(endpoint1, endpoint2));
    ComponentImpl component3 =
        new ComponentImpl(
            "chePlugin",
            "check/terminal-sample/0.0.1",
            ImmutableMap.of(
                "java.home",
                "/home/user/jdk11aertwertert",
                "java.boolean",
                "true",
                "java.long",
                "123444L"));
    MetadataImpl metadata = new MetadataImpl(name);
    metadata.setGenerateName(generatedName);
    DevfileImpl devfile =
        new DevfileImpl(
            "1.0.0",
            asList(project1, project2),
            asList(component1, component2, component3),
            asList(command1, command2),
            singletonMap("attribute1", "value1"),
            metadata);

    return devfile;
  }
}
