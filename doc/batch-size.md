# [graphql-provider]/Configure batch size

Graphql-provider automatically applies DataLoaders for all associated fields, the user can't cancel this behavior *(because not using DataLoader is harmful and not beneficial)*, so graphql-provider does not have performance problems caused by N+1 query.
