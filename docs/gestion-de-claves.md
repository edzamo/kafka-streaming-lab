# Gestión de claves y configuración en servicios con Kafka

Cómo se manejan las credenciales en los servicios documentados en [`case-studies/`](../case-studies) y cómo lo haría hoy.

## Cómo estaba resuelto

- Ninguna clave en el código: broker, política de acceso, clave, URLs y credenciales de Redis entran por **variables de entorno**.
- Una sola imagen para todos los ambientes; solo cambian las variables.
- En el desarrollo local, un perfil `local` aparte, que **no debe versionarse**.

## El error que hay que evitar

Un perfil `local` con la cadena de conexión real escrita en el archivo. Aunque sea un ambiente de desarrollo, la clave queda en el historial de git, en `build/` y dentro del `.jar` compilado. Se encontró exactamente eso al revisar estos proyectos, y se corrigió sustituyéndolo por variables y plantillas `*.example`.

## Cómo se autentica Event Hubs por el protocolo Kafka

```properties
security.protocol=SASL_SSL
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
  username="$ConnectionString" \
  password="Endpoint=sb://<host>/;SharedAccessKeyName=<politica>;SharedAccessKey=<clave>";
```

El usuario es el literal `$ConnectionString`; la contraseña es la cadena de conexión completa de una **política de acceso compartido**.

## Buenas prácticas

| Práctica | Por qué |
|---|---|
| Una política por tópico y por rol (`Send` o `Listen`) | Mínimo privilegio; si una clave se filtra, el daño es acotado |
| Secretos en un gestor (Key Vault) inyectados como variables | La clave no aparece en repos, imágenes ni logs |
| Rotación periódica de claves | Limita la vida útil de una filtración |
| Preferir Microsoft Entra ID / identidad administrada | Elimina las claves compartidas |
| `.gitignore` para `application-local.*`, `.env`, `build/` | Evita subir copias por accidente |
| Análisis de secretos en el repositorio y en el CI | Detecta el problema antes de que se publique |
| Plantillas `*.example` con placeholders | Documentan qué variables hacen falta sin exponer valores |

## Si una clave se filtró

1. **Revocarla o rotarla** en el proveedor. Borrar el archivo no la invalida.
2. Quitarla del código y de los artefactos compilados.
3. Limpiar el historial si ya se subió (`git filter-repo` o BFG), asumiendo que ya quedó comprometida.
4. Revisar los logs de acceso del recurso por uso indebido.
