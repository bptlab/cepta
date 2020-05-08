#### Internal types

Contains internal types that are fundamental building blocks of the generic high level models.

Let's consider `enum TransportType {...}` in `models/internal/types/transport.proto` 
for example, which is the type of a field in the generic `message Transport {...}`
model found in `models/internal/transport.proto`, but also cross-referenced by other 
models. Extracting those we avoid circular imports and have clearer hierarchies.

All interfaces are work in progress and open for discussion.