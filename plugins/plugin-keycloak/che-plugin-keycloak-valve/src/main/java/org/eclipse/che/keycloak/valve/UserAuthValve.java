package org.eclipse.che.keycloak.valve;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.eclipse.che.keycloak.shared.KeycloakSettings;
import org.keycloak.adapters.tomcat.KeycloakAuthenticatorValve;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

/**
 * Performs Keycloak and OpenShift.io user validation. Prompts user to login if necessary
 * and checks if the logged in user has access to the current Che instance.
 *
 * @see KeycloakAuthenticatorValve
 *
 * @author amisevsk
 */
public class UserAuthValve extends KeycloakAuthenticatorValve {

    synchronized void retrieveKeycloakSettingsIfNecessary(String apiEndpoint) {
        Map<String, String> keycloakSettings = KeycloakSettings.get();
        if (keycloakSettings == null) {
            KeycloakSettings.pullFromApiEndpointIfNecessary(apiEndpoint);
        }
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        retrieveKeycloakSettingsIfNecessary(request.getScheme() + "://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/api");
        super.invoke(request, response);
    }
}
