package org.eclipse.che.api.deploy;

import org.eclipse.che.api.core.util.SystemInfo;

import javax.inject.Provider;

/**
 * Provider api.endpoint url. This is used to make calls to che api from containers.
 * It may depend if we use boot2docker or not.
 *
 * @author Sergii Kabashniuk
 */
public class ApiEndpointProvider implements Provider<String> {
    @Override
    public String get() {
        if (SystemInfo.isMacOS()) {
            return "http://192.168.99.1:8080/che/api";
        }
        return "http://172.17.42.1:8080/che/api";
    }
}
