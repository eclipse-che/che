/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.devfile.server.validator;

import static java.lang.String.format;
import static org.eclipse.che.api.devfile.server.Constants.DOCKERIMAGE_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.PLUGIN_TOOL_TYPE;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.utils.Serialization;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.devfile.model.Action;
import org.eclipse.che.api.devfile.model.Command;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.model.Entrypoint;
import org.eclipse.che.api.devfile.model.Project;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.server.FileContentProvider;
import org.eclipse.che.api.devfile.server.convert.tool.kubernetes.ContainerSearch;
import org.eclipse.che.api.devfile.server.convert.tool.kubernetes.SelectorFilter;
import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.devfile.server.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;

/** Validates devfile logical integrity. */
@Singleton
public class DevfileIntegrityValidator {

  /**
   * Checks than name may contain only letters, digits, symbols _.- and does not starts with
   * non-word character.
   */
  private static final Pattern PROJECT_NAME_PATTERN = Pattern.compile("^[\\w\\d]+[\\w\\d_.-]*$");

  private final KubernetesRecipeParser kubernetesRecipeParser;

  @Inject
  public DevfileIntegrityValidator(KubernetesRecipeParser kubernetesRecipeParser) {
    this.kubernetesRecipeParser = kubernetesRecipeParser;
  }

  /**
   * Performs the following checks:
   *
   * <pre>
   * <ul>
   *   <li>All listed items (projects, tools, commands) have unique names</li>
   *   <li>There is only one tool of type cheEditor</li>
   *   <li>All tools exists which are referenced/required by command actions</li>
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
    Set<String> toolNames = validateTools(devfile);
    validateCommands(devfile, toolNames);
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
    try {
      for (Tool tool : devfile.getTools()) {
        List<HasMetadata> selectedObjects = validateSelector(tool, provider);
        validateEntrypointSelector(tool, selectedObjects);
      }
    } catch (Exception e) {
      throw new DevfileFormatException(
          format("Failed to validate content references: %s", e.getMessage()), e);
    }
  }

  private Set<String> validateTools(Devfile devfile) throws DevfileFormatException {
    Set<String> existingNames = new HashSet<>();
    Tool editorTool = null;
    Tool dockerimageTool = null;
    Tool k8sOSTool = null;
    for (Tool tool : devfile.getTools()) {
      if (!existingNames.add(tool.getName())) {
        throw new DevfileFormatException(format("Duplicate tool name found:'%s'", tool.getName()));
      }
      switch (tool.getType()) {
        case EDITOR_TOOL_TYPE:
          if (editorTool != null) {
            throw new DevfileFormatException(
                format(
                    "Multiple editor tools found: '%s', '%s'",
                    editorTool.getName(), tool.getName()));
          }
          editorTool = tool;
          break;

        case PLUGIN_TOOL_TYPE:
          // do nothing
          break;

        case KUBERNETES_TOOL_TYPE:
          // fall through
        case OPENSHIFT_TOOL_TYPE:
          if (dockerimageTool != null) {
            throw new DevfileFormatException(
                "Devfile cannot contain kubernetes/openshift and dockerimage tool at the same time");
          }
          k8sOSTool = tool;
          break;

        case DOCKERIMAGE_TOOL_TYPE:
          if (k8sOSTool != null) {
            throw new DevfileFormatException(
                "Devfile cannot contain kubernetes/openshift and dockerimage tool at the same time");
          }
          if (dockerimageTool != null) {
            throw new DevfileFormatException(
                "Devfile cannot contain multiple dockerimage tools at the same time");
          }
          dockerimageTool = tool;
          break;

        default:
          throw new DevfileFormatException(
              format("Unsupported tool '%s' type provided:'%s'", tool.getName(), tool.getType()));
      }
    }
    return existingNames;
  }

  private void validateCommands(Devfile devfile, Set<String> toolNames)
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

      if (!toolNames.contains(action.getTool())) {
        throw new DevfileFormatException(
            format(
                "Command '%s' has action that refers to non-existing tools '%s'",
                command.getName(), action.getTool()));
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

  /**
   * Validates that the selector, if any, selects some objects from the tool's referenced content.
   * Only does anything for kubernetes and openshift tools.
   *
   * @param tool the tool to check
   * @param contentProvider the content provider to use when fetching content
   * @return the list of referenced objects matching the selector or empty list
   * @throws ValidationException on failure to validate the referenced content
   * @throws InfrastructureException on failure to parse the referenced content
   * @throws IOException on failure to retrieve the referenced content
   * @throws DevfileException if the selector filters out all referenced objects
   */
  private List<HasMetadata> validateSelector(Tool tool, FileContentProvider contentProvider)
      throws ValidationException, InfrastructureException, IOException, DevfileException {

    if (!tool.getType().equals(KUBERNETES_TOOL_TYPE)
        && !tool.getType().equals(OPENSHIFT_TOOL_TYPE)) {
      return Collections.emptyList();
    }

    List<HasMetadata> content = getReferencedKubernetesList(tool, contentProvider);

    Map<String, String> selector = tool.getSelector();
    if (selector == null || selector.isEmpty()) {
      return content;
    }

    content = SelectorFilter.filter(content, selector);

    if (content.isEmpty()) {
      throw new DevfileException(
          format(
              "The selector of the tool %s filters out all objects from the list.",
              tool.getName()));
    }

    return content;
  }

  private void validateEntrypointSelector(Tool tool, List<HasMetadata> filteredObjects)
      throws DevfileException {

    if (tool.getEntrypoints() == null || tool.getEntrypoints().isEmpty()) {
      return;
    }

    for (Entrypoint ep : tool.getEntrypoints()) {
      ContainerSearch search =
          new ContainerSearch(ep.getParentName(), ep.getParentSelector(), ep.getContainerName());

      List<Container> cs = search.search(filteredObjects);

      if (cs.isEmpty()) {
        throw new DevfileFormatException(
            format(
                "Tool %s contains an entry point that doesn't match any container:\n%s",
                tool.getName(), toYAML(ep)));
      }
    }
  }

  private List<HasMetadata> getReferencedKubernetesList(
      Tool tool, FileContentProvider contentProvider)
      throws ValidationException, InfrastructureException, IOException, DevfileException {
    List<HasMetadata> content;
    if (tool.getLocalContent() != null) {
      content = kubernetesRecipeParser.parse(tool.getLocalContent());
    } else if (tool.getLocal() != null) {
      String data = contentProvider.fetchContent(tool.getLocal());
      content = kubernetesRecipeParser.parse(data);
    } else {
      content = Collections.emptyList();
    }

    return content;
  }

  private static String toYAML(Entrypoint ep) throws DevfileException {
    return Serialization.asYaml(ep);
  }
}
