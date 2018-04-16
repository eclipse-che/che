{{- define "imagePullSecret" }}
{{- if .Values.registry }}
{{- if and .Values.registry.password .Values.registry.username }}
{{- printf "{\"auths\": {\"%s\": {\"auth\": \"%s\"}}}" .Values.registry.host (printf "%s:%s" .Values.registry.username .Values.registry.password | b64enc) | b64enc }}
{{- end }}
{{- end }}
{{- end }}
