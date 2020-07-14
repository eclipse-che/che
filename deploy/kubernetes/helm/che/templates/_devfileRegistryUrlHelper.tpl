{{- define "devfileRegistryUrl" }}
  {{- if or (eq .Values.global.serverStrategy "default-host") (eq .Values.global.serverStrategy "single-host") }}
    {{- if .Values.global.tls.enabled }}
      {{- printf "https://%s/devfile-registry" .Values.global.ingressDomain }}
    {{- else }}
      {{- printf "http://%s/devfile-registry" .Values.global.ingressDomain }}
    {{- end }}
  {{- else }}
    {{- if .Values.global.tls.enabled }}
      https://{{ printf .Values.global.cheDevfileRegistryUrlFormat .Release.Namespace .Values.global.ingressDomain }}
    {{- else }}
      http://{{ printf .Values.global.cheDevfileRegistryUrlFormat .Release.Namespace .Values.global.ingressDomain }}
    {{- end }}
  {{- end }}
{{- end }}
