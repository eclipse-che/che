#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("hello")
public class ${yourPrefix}Service {

    @GET
    @Path("{name}")
    public String sayHello(@PathParam("name") String name) {
        return "Hello " + name + " !";
    }
}
