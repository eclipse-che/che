{{- define "cheHost" }}
{{- if .Values.global.isHostBased }}
{{- printf "master.%s" .Values.global.cheDomain }}
{{- else }}
{{- printf "%s" .Values.global.cheDomain }}
{{- end }}
{{- end }}
