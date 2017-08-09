package org.eclipse.che.workspace.infrastructure.openshift;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Sergii Leshchenko
 */
public class ServiceMatcher {
    private final List<Service> services;

    public static ServiceMatcher from(List<Service> services) {
        return new ServiceMatcher(services);
    }

    private ServiceMatcher(List<Service> services) {
        this.services = services;
    }

    public List<Service> match(Pod pod, Container container) {
        return services.stream()
                       .filter(service -> isExposedByService(pod, service))
                       .filter(service -> isExposedByService(container, service))
                       .collect(Collectors.toList());
    }

    private static boolean isExposedByService(Pod pod, Service service) {
        Map<String, String> labels = pod.getMetadata().getLabels();
        Map<String, String> selectorLabels = service.getSpec().getSelector();
        if (labels == null) {
            return false;
        }
        for (Map.Entry<String, String> selectorLabelEntry : selectorLabels.entrySet()) {
            if (!selectorLabelEntry.getValue().equals(labels.get(selectorLabelEntry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isExposedByService(Container container, Service service) {
        for (ServicePort servicePort : service.getSpec().getPorts()) {
            IntOrString targetPort = servicePort.getTargetPort();
            if (targetPort.getIntVal() != null) {
                for (ContainerPort containerPort : container.getPorts()) {
                    if (targetPort.getIntVal().equals(containerPort.getContainerPort())) {
                        return true;
                    }
                }
            } else {
                for (ContainerPort containerPort : container.getPorts()) {
                    if (targetPort.getStrVal().equals(containerPort.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
