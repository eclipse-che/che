package org.eclipse.che.core.metrics;

import static com.jayway.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Mockito.verify;

import com.jayway.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(EverrestJetty.class)
public class ServerErrorResponceMetricFilterTest {


  @Mock
  ServerErrorCounter serverErrorCounter;

  @InjectMocks
  private ServerErrorResponceMetricsFilter filter;

  @Test(dataProvider = "serverErrors")
  public void shouldIncrementCounterOnServerErrorResponces(int status) {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .put(SECURE_PATH + "/service");

    verify(serverErrorCounter).incrementCounter();
  }

  @Path("/service")
  public static class DummyService {

    @GET
    @Path("/test")
    @Produces(APPLICATION_JSON)
    public void testMethod() {}
  }

  @DataProvider(name = "serverErrors")
  public Object[][] serverErrors() {
    return new Object[][]{ {500}, {501}, {502}, {503}, {504}};
  }
}
