package op

import "github.com/eclipse/che/exec-agent/rest"

var HttpRoutes = rest.RoutesGroup{
	"Channel Routes",
	[]rest.Route{
		{
			"GET",
			"Connect to Machine-Agent(webscoket)",
			"/connect",
			registerChannel,
		},
	},
}
