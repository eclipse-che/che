{{- define "cheHost" }}
{{- if eq .Values.global.serverStrategy "default-host" }}
{{- printf "%s" .Values.global.ingressDomain }}
{{- else if eq .Values.global.serverStrategy "single-host" }}
{{- printf "che.%s" .Values.global.ingressDomain }}
{{- else }}
{{- printf "master.%s" .Values.global.ingressDomain }}
{{- end }}
{{- end }}
