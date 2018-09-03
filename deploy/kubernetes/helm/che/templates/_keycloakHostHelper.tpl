{{- define "keycloakHost" }}
{{- if or (eq .Values.global.serverStrategy "default-host") (eq .Values.global.serverStrategy "single-host") }}
{{- printf "%s" .Values.global.ingressDomain }}
{{- else }}
{{- printf "keycloak-%s.%s" .Release.Namespace .Values.global.ingressDomain }}
{{- end }}
{{- end }}