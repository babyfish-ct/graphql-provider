rootProject.name = "graphql-provider"
include("kimmer-ksp")
findProject(":kimmer-ksp")?.name = "graphql-provider-kimmer-ksp"
include("kimmer")
findProject(":kimmer")?.name = "graphql-provider-kimmer"
include("r2dbc")
findProject(":r2dbc")?.name = "graphql-provider-r2dbc"
include("server")
findProject(":server")?.name = "graphql-provider-server"
