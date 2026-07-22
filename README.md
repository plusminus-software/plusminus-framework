# Plusminus Framework

Framework based on Spring Boot and Plusminus software

Plusminus Framework is the batteries-included aggregate of the Plusminus ecosystem: adding this single dependency pulls in the core Plusminus modules together with Spring Boot's JPA starter, so a new service gets persistence, multitenancy, soft deletion, auditing, sync and security wired up out of the box.

## What's included

Compile-scope dependencies bundled by this artifact:

| Module | What it gives you |
| --- | --- |
| `spring-boot-starter-data-jpa` | Spring Data JPA persistence stack |
| `plusminus-http` | HTTP utilities |
| `plusminus-json` | JSON serialization support |
| `plusminus-data` | Data layer with `CrudRepository` base |
| `plusminus-softdelete` | Soft deletion of entities |
| `plusminus-tenant` | Multitenancy support |
| `plusminus-audit` | Audit logging of entity changes |
| `plusminus-sync` | Sync endpoint for entity synchronization |
| `plusminus-security` | Security integration |

## Autoconfiguration

The module registers `FrameworkAutoconfig` via `META-INF/spring.factories`, so it activates automatically in any Spring Boot application. It also provides:

- `AbstractEntity` — a `@MappedSuperclass` base entity with `id`, unique `uuid`, `tenant`, soft-delete flag, optimistic-locking `version` and `modifiedTime` fields, pre-annotated with `@Auditable`, `@Syncable` and Hibernate filters for tenant and soft-delete scoping.
- `AbstractRepository<T>` — a `@NoRepositoryBean` base interface extending the Plusminus `CrudRepository` for `AbstractEntity` subclasses.
- `AuditingConfig` — enables JPA auditing with a `ZonedDateTime`-compatible `DateTimeProvider` and a security-context-based `AuditorAware`.

Integration tests verify the assembled stack end to end: CRUD operations, audit log, the sync endpoint, data events, module interoperability (type, patch, listener, inject, job, collection, file/S3), code generation, and browser tests covering the admin panel and login flow.

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
