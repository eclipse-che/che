{{- define "keycloakAuthUrl" }}
{{- if .Values.global.isHostBased }}
{{- if .Values.global.tlsEnabled }}
{{- printf "https://keycloak.%s/auth" .Values.global.cheDomain }}
{{- else }}
{{- printf "http://keycloak.%s/auth" .Values.global.cheDomain }}
{{- end }}
{{- else }}
{{- if .Values.global.tlsEnabled }}
{{- printf "https://%s/auth" .Values.global.cheDomain }}
{{- else }}
{{- printf "http://%s/auth" .Values.global.cheDomain }}
{{- end }}
{{- end }}
{{- end }}
