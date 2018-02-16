{{- define "cheHost" }}
{{- if .Values.isHostBased }}
{{- printf "master.%s" .Values.cheDomain }}
{{- else }}
{{- printf "%s" .Values.cheDomain }}
{{- end }}
{{- end }}
