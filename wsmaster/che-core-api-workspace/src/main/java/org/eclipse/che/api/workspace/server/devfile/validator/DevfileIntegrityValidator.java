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
package org.eclipse.che.api.workspace.server.devfile.validator;

import static java.lang.String.format;
import static org.eclipse.che.api.workspace.server.devfile.Components.getIdentifiableComponentName;
import static org.eclipse.che.api.workspace.server.devfile.Constants.DOCKERIMAGE_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.OPENSHIFT_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PLUGIN_COMPONENT_TYPE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.devfile.Action;
import org.eclipse.che.api.core.model.workspace.devfile.Command;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.core.model.workspace.devfile.Endpoint;
import org.eclipse.che.api.core.model.workspace.devfile.Env;
import org.eclipse.che.api.core.model.workspace.devfile.Project;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;

/** Validates devfile logical integrity. */
@Singleton
public class DevfileIntegrityValidator {

  /**
   * Checks than name may contain only letters, digits, symbols _.- and does not starts with
   * non-word character.
   */
  private static final Pattern PROJECT_NAME_PATTERN = Pattern.compile("^[\\w\\d]+[\\w\\d_.-]*$");

  private final Map<String, ComponentIntegrityValidator> validators;

  @Inject
  public DevfileIntegrityValidator(Map<String, ComponentIntegrityValidator> validators) {
    this.validators = validators;
  }

  /**
   * Performs the following checks:
   *
   * <pre>
   * <ul>
   *   <li>All listed items (projects, components, commands) have unique names</li>
   *   <li>There is only one component of type cheEditor</li>
   *   <li>All components exists which are referenced/required by command actions</li>
   *   <li>Project names conforms naming rules</li>
   * </ul>
   * </pre>
   *
   * <p>Note that this doesn't validate the selectors in the devfile. If you have access to the
   * {@link FileContentProvider} instance, you may want to also invoke {@link
   * #validateContentReferences(Devfile, FileContentProvider)} to perform further validation that is
   * otherwise impossible.
   *
   * @param devfile input devfile
   * @throws DevfileFormatException if some of the checks is failed
   * @see #validateContentReferences(Devfile, FileContentProvider)
   */
  public void validateDevfile(Devfile devfile) throws DevfileFormatException {
    validateProjects(devfile);
    Set<String> knownAliases = validateComponents(devfile);
    validateCommands(devfile, knownAliases);
  }

  /**
   * Validates that various selectors in the devfile reference something in the referenced content.
   *
   * @param devfile the validated devfile
   * @param provider the file content provider to fetch the referenced content with.
   * @throws DevfileFormatException when some selectors don't match any objects in the referenced
   *     content or any other exception thrown during the validation
   */
  public void validateContentReferences(Devfile devfile, FileContentProvider provider)
      throws DevfileFormatException {
    for (Component component : devfile.getComponents()) {
      ComponentIntegrityValidator validator = validators.get(component.getType());
      if (validator == null) {
        throw new DevfileFormatException(format("Unknown component type: %s", component.getType()));
      }

      validator.validateComponent(component, provider);
    }
  }

  private Set<String> validateComponents(Devfile devfile) throws DevfileFormatException {
    Set<String> definedAliases = new HashSet<>();
    Component editorComponent = null;

    Map<String, Set<String>> idsPerComponentType = new HashMap<>();

    for (Component component : devfile.getComponents()) {
      if (component.getAlias() != null && !definedAliases.add(component.getAlias())) {
        throw new DevfileFormatException(
            format("Duplicate component alias found:'%s'", component.getAlias()));
      }
      Optional<Map.Entry<String, Long>> duplicatedEndpoint =
          component
              .getEndpoints()
              .stream()
              .map(Endpoint::getName)
              .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
              .entrySet()
              .stream()
              .filter(e -> e.getValue() > 1L)
              .findFirst();
      if (duplicatedEndpoint.isPresent()) {
        throw new DevfileFormatException(
            format(
                "Duplicated endpoint name '%s' found in '%s' component",
                duplicatedEndpoint.get().getKey(), getIdentifiableComponentName(component)));
      }

      Set<String> tempSet = new HashSet<>();
      for (Env env : component.getEnv()) {
        if (!tempSet.add(env.getName())) {
          throw new DevfileFormatException(
              format(
                  "Duplicate environment variable '%s' found in component '%s'",
                  env.getName(), getIdentifiableComponentName(component)));
        }
      }

      if (!idsPerComponentType
          .computeIfAbsent(component.getType(), __ -> new HashSet<>())
          .add(getIdentifiableComponentName(component))) {

        throw new DevfileFormatException(
            format(
                "There are multiple components '%s' of type '%s' that cannot be uniquely"
                    + " identified. Please add aliases that would distinguish the components.",
                getIdentifiableComponentName(component), component.getType()));
      }

      if (component.getAutomountWorkspaceSecrets() != null && component.getAlias() == null) {
        throw new DevfileFormatException(
            format(
                "The 'automountWorkspaceSecrets' property cannot be used in component which doesn't have alias. "
                    + "Please add alias to component '%s' that would allow to distinguish its containers.",
                getIdentifiableComponentName(component)));
      }

      switch (component.getType()) {
        case EDITOR_COMPONENT_TYPE:
          if (editorComponent != null) {
            throw new DevfileFormatException(
                format(
                    "Multiple editor components found: '%s', '%s'",
                    getIdentifiableComponentName(editorComponent),
                    getIdentifiableComponentName(component)));
          }
          editorComponent = component;
          break;

        case PLUGIN_COMPONENT_TYPE:
        case KUBERNETES_COMPONENT_TYPE:
        case OPENSHIFT_COMPONENT_TYPE:
        case DOCKERIMAGE_COMPONENT_TYPE:
          // do nothing
          break;

        default:
          throw new DevfileFormatException(
              format(
                  "One of the components has unsupported component type: '%s'",
                  component.getType()));
      }
    }
    return definedAliases;
  }

  private void validateCommands(Devfile devfile, Set<String> knownAliases)
      throws DevfileFormatException {
    Set<String> existingNames = new HashSet<>();
    for (Command command : devfile.getCommands()) {
      if (!existingNames.add(command.getName())) {
        throw new DevfileFormatException(
            format("Duplicate command name found:'%s'", command.getName()));
      }

      if (command.getActions().isEmpty()) {
        throw new DevfileFormatException(
            format("Command '%s' does not have actions.", command.getName()));
      }

      // It is temporary restriction while exec plugin is not able to handle multiple commands
      // in different containers as one Task. Later supporting of multiple actions in one command
      // may be implemented.
      if (command.getActions().size() > 1) {
        throw new DevfileFormatException(
            format("Multiple actions in command '%s' are not supported yet.", command.getName()));
      }

      Action action = command.getActions().get(0);

      if (action.getComponent() == null
          && (action.getReference() != null || action.getReferenceContent() != null)) {
        // ok, this action contains a reference to the file containing the definition. Such
        // actions don't have to have component alias defined.
        continue;
      }

      if (!knownAliases.contains(action.getComponent())) {
        throw new DevfileFormatException(
            format(
                "Command '%s' has action that refers to a component with unknown alias '%s'",
                command.getName(), action.getComponent()));
      }
    }
  }

  private void validateProjects(Devfile devfile) throws DevfileFormatException {
    Set<String> existingNames = new HashSet<>();
    for (Project project : devfile.getProjects()) {
      if (!existingNames.add(project.getName())) {
        throw new DevfileFormatException(
            format("Duplicate project name found:'%s'", project.getName()));
      }
      if (!PROJECT_NAME_PATTERN.matcher(project.getName()).matches()) {
        throw new DevfileFormatException(
            format(
                "Invalid project name found:'%s'. Name must contain only Latin letters,"
                    + "digits or these following special characters ._-",
                project.getName()));
      }
    }
  }
}
