import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
/**
 * Created by sudaraka on 7/7/17.
 */
@Path("datasource")
public class DatasourceService {
    @GET
    @Path("{name}")
    public String sayHello(@PathParam("name") String name) {
        return "Hello " + name + "!";
    }
}
