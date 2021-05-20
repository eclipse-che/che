/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile.validator;

import static org.eclipse.che.api.workspace.server.devfile.Constants.DOCKERIMAGE_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.OPENSHIFT_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PLUGIN_COMPONENT_TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ActionImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EnvImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ProjectImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

@Listeners(MockitoTestNGListener.class)
public class DevfileIntegrityValidatorTest {

  private ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

  private DevfileImpl initialDevfile;

  private DevfileIntegrityValidator integrityValidator;

  @Mock private ComponentIntegrityValidator dummyComponentValidator;

  @BeforeClass
  public void setUp() throws Exception {
    Map<String, ComponentIntegrityValidator> componentValidators = new HashMap<>();
    componentValidators.put(KUBERNETES_COMPONENT_TYPE, dummyComponentValidator);
    componentValidators.put(OPENSHIFT_COMPONENT_TYPE, dummyComponentValidator);
    componentValidators.put(PLUGIN_COMPONENT_TYPE, dummyComponentValidator);
    componentValidators.put(EDITOR_COMPONENT_TYPE, dummyComponentValidator);

    integrityValidator = new DevfileIntegrityValidator(componentValidators);
    String devFileYamlContent =
        Files.readFile(getClass().getClassLoader().getResourceAsStream("devfile/devfile.yaml"));
    initialDevfile = objectMapper.readValue(devFileYamlContent, DevfileImpl.class);
  }

  @Test
  public void shouldValidateCorrectDevfile() throws Exception {
    // when
    integrityValidator.validateDevfile(initialDevfile);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Duplicate component alias found:'mvn-stack'")
  public void shouldThrowExceptionOnDuplicateComponentAlias() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);
    ComponentImpl component = new ComponentImpl();
    component.setAlias(initialDevfile.getComponents().get(0).getAlias());
    broken.getComponents().add(component);
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Duplicated endpoint name 'e1' found in 'dockerimage:latest' component")
  public void shouldThrowExceptionOnDuplicateEndpointName() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);
    ComponentImpl component = new ComponentImpl();
    component.setType(DOCKERIMAGE_COMPONENT_TYPE);
    component.setImage("dockerimage:latest");
    component.setEndpoints(
        ImmutableList.of(
            new EndpointImpl("e1", 8080, Collections.emptyMap()),
            new EndpointImpl("e1", 8082, Collections.emptyMap())));
    broken.getComponents().add(component);
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Multiple editor components found: 'eclipse/che-theia/0.0.3', 'editor-2'")
  public void shouldThrowExceptionOnMultipleEditors() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);
    ComponentImpl component = new ComponentImpl();
    component.setAlias("editor-2");
    component.setType(EDITOR_COMPONENT_TYPE);
    broken.getComponents().add(component);
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Duplicate command name found:'build'")
  public void shouldThrowExceptionOnDuplicateCommandName() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);

    CommandImpl command = new CommandImpl();
    command.setName(initialDevfile.getCommands().get(0).getName());
    broken.getCommands().add(command);
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Command 'build' does not have actions.")
  public void shouldThrowExceptionWhenCommandDoesNotHaveActions() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);
    broken.getCommands().get(0).getActions().clear();

    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Multiple actions in command 'build' are not supported yet.")
  public void shouldThrowExceptionWhenCommandHasMultipleActions() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);
    broken.getCommands().get(0).getActions().add(new ActionImpl());

    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Command 'build' has action that refers to a component with unknown alias 'no_such_component'")
  public void shouldThrowExceptionOnUnexistingCommandActionComponent() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);
    broken.getCommands().get(0).getActions().clear();
    ActionImpl action = new ActionImpl();
    action.setComponent("no_such_component");
    broken.getCommands().get(0).getActions().add(action);

    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Duplicate project name found:'petclinic'")
  public void shouldThrowExceptionOnDuplicateProjectName() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);
    ProjectImpl project = new ProjectImpl();
    project.setName(initialDevfile.getProjects().get(0).getName());
    broken.getProjects().add(project);
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Duplicate environment variable 'foo' found in component 'test1'")
  public void shouldThrowExceptionOnDuplicateEnvironmentVariable() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);
    ComponentImpl k8s1 = new ComponentImpl();
    k8s1.setType(OPENSHIFT_COMPONENT_TYPE);
    k8s1.setAlias("test1");
    k8s1.getEnv().add(new EnvImpl("foo", "bar"));
    k8s1.getEnv().add(new EnvImpl("foo", "baz"));
    broken.getComponents().add(k8s1);
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Invalid project name found:'.*'. Name must contain only Latin letters,"
              + "digits or these following special characters ._-")
  public void shouldThrowExceptionOnInvalidProjectName() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);
    broken.getProjects().get(0).setName("./" + initialDevfile.getProjects().get(0).getName());
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test
  public void shouldNotValidateContentReferencesOnNonKuberenetesComponents() throws Exception {
    // given

    // just remove all the content-referencing components and check that all still works
    DevfileImpl devfile = new DevfileImpl(initialDevfile);
    Iterator<ComponentImpl> it = devfile.getComponents().iterator();
    while (it.hasNext()) {
      String componentType = it.next().getType();
      if (componentType.equals(KUBERNETES_COMPONENT_TYPE)
          || componentType.equals(OPENSHIFT_COMPONENT_TYPE)) {
        it.remove();
      }
    }

    // when
    integrityValidator.validateContentReferences(devfile, __ -> "");

    // then
    // no exception is thrown
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "There are multiple components 'dockerimage:latest' of type 'dockerimage' that cannot be"
              + " uniquely identified. Please add aliases that would distinguish the components.")
  public void shouldRequireAliasWhenDockerImageComponentsHaveSameImage() throws Exception {
    // given
    DevfileImpl devfile = new DevfileImpl(initialDevfile);

    ComponentImpl docker1 = new ComponentImpl();
    docker1.setType(DOCKERIMAGE_COMPONENT_TYPE);
    docker1.setImage("dockerimage:latest");

    ComponentImpl docker2 = new ComponentImpl();
    docker2.setType(DOCKERIMAGE_COMPONENT_TYPE);
    docker2.setImage("dockerimage:latest");

    devfile.getComponents().add(docker1);
    devfile.getComponents().add(docker2);

    // when
    integrityValidator.validateDevfile(devfile);

    // then
    // exception is thrown
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "There are multiple components 'list.yaml' of type 'kubernetes' that cannot be"
              + " uniquely identified. Please add aliases that would distinguish the components.")
  public void shouldRequireAliasWhenKubernetesComponentsHaveSameReference() throws Exception {
    // given
    DevfileImpl devfile = new DevfileImpl(initialDevfile);

    ComponentImpl k8s1 = new ComponentImpl();
    k8s1.setType(KUBERNETES_COMPONENT_TYPE);
    k8s1.setReference("list.yaml");

    ComponentImpl k8s2 = new ComponentImpl();
    k8s2.setType(KUBERNETES_COMPONENT_TYPE);
    k8s2.setReference("list.yaml");

    devfile.getComponents().add(k8s1);
    devfile.getComponents().add(k8s2);

    // when
    integrityValidator.validateDevfile(devfile);

    // then
    // exception is thrown
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "There are multiple components 'list.yaml' of type 'openshift' that cannot be"
              + " uniquely identified. Please add aliases that would distinguish the components.")
  public void shouldRequireAliasWhenOpenshiftComponentsHaveSameReference() throws Exception {
    // given
    DevfileImpl devfile = new DevfileImpl(initialDevfile);

    ComponentImpl k8s1 = new ComponentImpl();
    k8s1.setType(OPENSHIFT_COMPONENT_TYPE);
    k8s1.setReference("list.yaml");

    ComponentImpl k8s2 = new ComponentImpl();
    k8s2.setType(OPENSHIFT_COMPONENT_TYPE);
    k8s2.setReference("list.yaml");

    devfile.getComponents().add(k8s1);
    devfile.getComponents().add(k8s2);

    // when
    integrityValidator.validateDevfile(devfile);

    // then
    // exception is thrown
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "There are multiple components 'openshift' of type 'openshift' that cannot be"
              + " uniquely identified. Please add aliases that would distinguish the components.")
  public void shouldRequireAliasWhenOpenshiftComponentsHaveNoReference() throws Exception {
    // given
    DevfileImpl devfile = new DevfileImpl(initialDevfile);

    ComponentImpl k8s1 = new ComponentImpl();
    k8s1.setType(OPENSHIFT_COMPONENT_TYPE);

    ComponentImpl k8s2 = new ComponentImpl();
    k8s2.setType(OPENSHIFT_COMPONENT_TYPE);

    devfile.getComponents().add(k8s1);
    devfile.getComponents().add(k8s2);

    // when
    integrityValidator.validateDevfile(devfile);

    // then
    // exception is thrown
  }
}
