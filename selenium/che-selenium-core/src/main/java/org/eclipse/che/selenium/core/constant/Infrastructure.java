package org.eclipse.che.selenium.core.constant;

import com.google.inject.Singleton;

@Singleton
public enum Infrastructure {
    DOCKER, OPENSHIFT, K8S
}
