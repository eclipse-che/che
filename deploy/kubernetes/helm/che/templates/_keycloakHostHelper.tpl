{{- define "keycloakHost" }}
{{- if .Values.global.isHostBased }}
{{- printf "keycloak.%s" .Values.global.cheDomain }}
{{- else }}
{{- printf "%s" .Values.global.cheDomain }}
{{- end }}
{{- end }}
