FROM jboss/keycloak-openshift:3.3.0.CR2-3
ADD che-realm.json.erb che-users-0.json.erb /scripts/
USER root
RUN chgrp -R 0 /scripts && \
    chmod -R g+rwX /scripts
ENTRYPOINT ["/scripts/keycloak_config.sh"]