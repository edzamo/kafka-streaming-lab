# async-interbank-transfer

Microservicio consumidor de eventos Kafka (Azure Event Hubs) que orquesta la ejecución asíncrona de transferencias interbancarias. Proyecto de **portafolio/documentación**, anonimizado. Estado: **migración de arquitectura en capas a hexagonal** (rama `feature/interbank-hexagonal`). Caso de estudio: [`../case-studies/async-interbank-transfer.md`](../case-studies/async-interbank-transfer.md).

> Estándar de ingeniería: kit central en `~/.claude/kit` (agentes, comandos, skills genéricos). Contrato: `~/.claude/kit/PROJECT-CONTRACT.md`. Este archivo solo declara lo propio del proyecto.

## Stack
- Lenguaje / framework / versión: Java 21, Spring Boot 3.5.x (WebFlux, Reactor), Spring Cloud Stream (binder Kafka), Spring Data Redis reactivo, Gradle (Groovy DSL).
- Clientes HTTP: generados desde los OpenAPI de `src/main/resources` con `openapi-generator` estándar (librería `webclient`). Ya no se usa el generador interno original.
- Datos / hosting: sin BD propia. Redis (pub/sub) y Azure Event Hubs por protocolo Kafka. Configuración solo por variables de entorno.

## Arquitectura
Estilo: hexagonal

Paquete base: `com.example.banking.interbank` → `domain`, `application.{in,out,service}`, `adapter.{in,out}`, clase de arranque.

## Comandos
```bash
# tests:
./gradlew test
# arquitectura (lint:arch): test ArchUnit dentro de la suite
./gradlew test --tests '*ArchitectureTest'
# build:
./gradlew build
```

## Skills del proyecto (los leen los agentes y los comandos)
| Skill | Cuándo |
|---|---|
| `~/.claude/kit/.claude/skills/stacks/java/index.md` | **Siempre**: reglas del stack Java/Spring |
| `~/.claude/kit/.claude/skills/architecture/hexagonal/rules.md` | **Siempre**: reglas hexagonales (INV-01..18) |
| `~/.claude/kit/.claude/skills/quality/owasp-security/checklists.md` | Seguridad (obligatorio para `security-agent`) |

## Convenciones
- Idioma: código y nombres en inglés; documentación en español.
- Nombres: puertos de entrada con verbo de negocio; puertos de salida por rol (sin `Kafka`, `Redis`, `Http` en el nombre); adaptadores `<Rol>Client` / `<Rol>Publisher` / `<Rol>Notifier`.
- Tests (dónde viven): `src/test/java` espejo del paquete de producción; fakes en memoria en `src/test/java/.../fake`.
- Secretos: nunca en el repositorio; variables de entorno (ver `docs/gestion-de-claves.md`).

## Deuda conocida / excepciones autorizadas
`EXC-<n> | <regla/hallazgo> | <motivo> | <autorizó> | <AAAA-MM-DD>`
EXC-1 | `reactor.` prohibido en el core (invariants.json) | Todo el flujo es WebFlux; los puertos de salida y entrada devuelven `Mono`; el dominio sigue sin Reactor | edzamo | 2026-10-07
EXC-2 | offset confirmado antes de terminar el flujo (riesgo de pérdida del evento durante `Mono.delay`) y notificación Redis best-effort | PoC de portafolio; el consumidor reactivo hace `subscribe` único | edzamo | 2026-10-07
