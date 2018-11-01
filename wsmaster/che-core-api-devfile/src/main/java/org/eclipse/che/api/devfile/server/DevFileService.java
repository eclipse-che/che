package org.eclipse.che.api.devfile.server;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.eclipse.che.api.core.rest.Service;

@Path("/devfile")
public class DevFileService extends Service {

//  //Creates a workspace by providing the url to the repository
//  @POST
//  @Produces(APPLICATION_JSON)
//  public Response create(@QueryParam("repo_url") String repo_url){
//  }
//
//
//  // Generates a workspace by sending a devfile to a rest API
//  // Initially this method will return empty workspace configuration.
//  // And will start che-devfile-broker on a background to clone sources and get devfile.
//  @POST
//  @Consumes("text/yml")
//  @Produces(APPLICATION_JSON)
//  public Response createFromYaml(DevFile defFile){
//  }
//


  // Generates the devfile based on an existing workspace
  // key = workspace12345678
  // key = namespace/workspace_name
  // key = namespace_part_1/namespace_part_2/workspace_name
  // See get workspace by id aka key.
  @GET
  @Path("/{key:.*}")
  @Produces("text/yml")
  public Response createFromWorkspace(@PathParam("key") String key){
  }

}
