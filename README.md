# Plusminus Framework

Framework based on Spring Boot and Plusminus software

Plusminus Framework is the batteries-included aggregate of the Plusminus ecosystem: adding this single dependency pulls in the core Plusminus modules together with Spring Boot's JPA starter, so a new service gets persistence, multitenancy, soft deletion, auditing, sync and security wired up out of the box.

## What's included

Compile-scope dependencies bundled by this artifact:

| Module | What it gives you |
| --- | --- |
| `spring-boot-starter-data-jpa` | Spring Data JPA persistence stack |
| `plusminus-admin` | Out-of-the-box CRUD admin panel for `@Admin` types |
| `plusminus-http` | HTTP request/response contexts and the request scope |
| `plusminus-transactional` | Transaction-bound contexts |
| `plusminus-data` | Data layer with `CrudRepository`/`CrudController` bases |
| `plusminus-softdelete` | Soft-delete read filtering |
| `plusminus-tenant` | Multitenancy support |
| `plusminus-audit` | Audit logging of entity changes |
| `plusminus-sync` | Sync endpoint for entity synchronization |
| `plusminus-s3` | S3-backed `FileService` and the `/files` controller |
| `plusminus-security` | Security integration (authentication, authorization, JWT, login, users) |
| `plusminus-spring` | Static `ApplicationContext` access and generic-type bean helpers |
| `plusminus-utils` | Reflection and object-graph utilities |

`plusminus-json`, `plusminus-dehydration`, `plusminus-metadata`, `plusminus-patch`,
`plusminus-hibernate`, `plusminus-data-event`, `plusminus-context`, `plusminus-scope`
and `plusminus-type` arrive transitively through the modules above.

`plusminus-test`, `plusminus-collection`, `plusminus-generator` and `plusminus-job` are
test-scope here — depend on them directly if your application needs them at runtime.

## Autoconfiguration

The module registers `FrameworkAutoconfig` via `META-INF/spring.factories`, so it activates automatically in any Spring Boot application. It also provides:

- `AbstractEntity` — a `@MappedSuperclass` base entity with `id`, unique `uuid`, `tenant`, soft-delete flag, optimistic-locking `version` and `modifiedTime` fields, pre-annotated with `@Auditable`, `@Syncable` and Hibernate filters for tenant and soft-delete scoping.
- `AbstractRepository<T>` — a `@NoRepositoryBean` base interface extending the Plusminus `CrudRepository` for `AbstractEntity` subclasses.
- `AuditingConfig` — enables JPA auditing with a `ZonedDateTime`-compatible `DateTimeProvider` and a security-context-based `AuditorAware`.

Integration tests verify the assembled stack end to end: CRUD operations, audit log, the sync
endpoint, data events, dehydration, Hibernate filters, HTTP/scope/transactional contexts, module
interoperability (type, patch, job, collection, spring, metadata, utils, file/S3), code generation,
and browser tests covering the admin panel and login flow.

## Getting started

```xml
<dependency>
    <groupId>software.plusminus</groupId>
    <artifactId>plusminus-framework</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Building

Requires JDK 8.

```bash
./mvnw clean install
```

The build enforces Checkstyle, PMD, SpotBugs and JaCoCo coverage checks.

## License

Licensed under the Apache License, Version 2.0 — see [LICENSE](LICENSE).
