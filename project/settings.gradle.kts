rootProject.name = "graphql-provider"
include("r2dbc")
findProject(":r2dbc")?.name = "graphql-provider-r2dbc"
include("kimmer")
findProject(":kimmer")?.name = "graphql-provider-kimmer"
