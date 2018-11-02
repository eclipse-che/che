package org.eclipse.che.api.devfile.server;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.devfile.model.Command;
import org.eclipse.che.api.devfile.model.DevFile;
import org.eclipse.che.api.devfile.model.Project;
import org.eclipse.che.api.devfile.model.Source;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;


@Path("/devfile")
public class DevFileService extends Service {

  private static final String SPEC_VERSION = "0.0.1";
  private WorkspaceManager workspaceManager;

  @Inject
  public DevFileService(WorkspaceManager workspaceManager) {
    this.workspaceManager = workspaceManager;
  }

  //Creates a workspace by providing the url to the repository
  @POST
  @Produces(APPLICATION_JSON)
  public Response create(@QueryParam("repo_url") String repo_url){
  }


  // Generates a workspace by sending a devfile to a rest API
  // Initially this method will return empty workspace configuration.
  // And will start che-devfile-broker on a background to clone sources and get devfile.
  @POST
  @Consumes("text/yml")
  @Produces(APPLICATION_JSON)
  public Response createFromYaml(DevFile defFile){
  }



  // Generates the devfile based on an existing workspace
  // key = workspace12345678
  // key = namespace/workspace_name
  // key = namespace_part_1/namespace_part_2/workspace_name
  // See get workspace by id aka key.
  @GET
  @Path("/{key:.*}")
  @Produces("text/yml")
  public Response createFromWorkspace(@PathParam("key") String key)
      throws NotFoundException, ServerException {
    //TODO: validate key
    WorkspaceImpl workspace = workspaceManager.getWorkspace(key);
    DevFile workspaceDevFile = workspaceToDevFile(workspace);
  }


  private DevFile workspaceToDevFile(WorkspaceImpl workspace) {
    DevFile devFile  = new DevFile();
    devFile.setSpecVersion(SPEC_VERSION);
    devFile.setName("???");

    // Manage projects
    List<Project> projects = new ArrayList<>();
    for (ProjectConfigImpl project : workspace.getConfig().getProjects()) {
      Project devProject = new Project();
      devProject.setName(project.getName());
      Source source = new Source();
      source.setType(project.getSource().getType());
      source.setLocation(project.getSource().getLocation());
      devProject.setSource(source);
      projects.add(devProject);
    }
    devFile.setProjects(projects);


    // Manage commands
    List<Command> commands = new ArrayList<>();
    for (CommandImpl command : workspace.getConfig().getCommands()) {
      Command devCommand = new Command();
      devCommand.setName(command.getName());
      
      devCommand.setToolsCommands();
      commands.add(devCommand);
    }
    devFile.setCommands(commands);





    devFile.setTools();
  }

}
