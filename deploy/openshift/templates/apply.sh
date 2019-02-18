#!/bin/bash
set -e
oc login -u system:admin
oc project eclipse-che
oc process -f che-monitoring.yaml | oc apply -f -
oc rollout latest dc/grafana -n eclipse-che