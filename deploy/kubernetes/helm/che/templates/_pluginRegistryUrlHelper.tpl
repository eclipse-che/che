{{- define "pluginRegistryUrl" }}
  {{- if or (eq .Values.global.serverStrategy "default-host") (eq .Values.global.serverStrategy "single-host") }}
    {{- if .Values.global.tls.enabled }}
      {{- printf "https://%s/plugin-registry/v3" .Values.global.ingressDomain }}
    {{- else }}
      {{- printf "http://%s/plugin-registry/v3" .Values.global.ingressDomain }}
    {{- end }}
  {{- else }}
    {{- if .Values.global.tls.enabled }}
      https://{{ printf .Values.global.chePluginRegistryUrlFormat .Release.Namespace .Values.global.ingressDomain }}/v3
    {{- else }}
      http://{{ printf .Values.global.chePluginRegistryUrlFormat .Release.Namespace .Values.global.ingressDomain }}/v3
    {{- end }}
  {{- end }}
{{- end }}
