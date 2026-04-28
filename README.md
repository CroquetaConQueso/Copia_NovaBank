# NovaBank Digital Services - Modulo 3

NovaBank Digital Services es un backend REST bancario desarrollado con Spring Boot. Este modulo migra la aplicacion de consola con JDBC manual del Modulo 2 a una API HTTP con arquitectura por capas, Spring Data JPA, Spring Security, JWT, OpenAPI/Swagger, DTOs y testing por capas.

## Funcionalidades

- Gestion de clientes: alta, consulta por id y listado.
- Gestion de cuentas: creacion, consulta por id y consulta por cliente.
- Operaciones financieras: deposito, retirada y transferencia.
- Consulta de movimientos por cuenta.
- Autenticacion con JWT.
- Documentacion interactiva con Swagger UI.

## Arquitectura

```text
com.novabank
|-- NovaBankApplication
|-- config
|-- controller
|-- dto
|-- exception
|-- mapper
|-- model
|-- repository
|-- security
|-- service
    |-- strategy
```

Responsabilidades principales:

- `controller`: endpoints REST, sin try/catch de negocio.
- `dto`: contratos publicos de entrada y salida. No contienen anotaciones JPA.
- `mapper`: conversion manual entre entidades y DTOs. No acceden a repositorios.
- `model`: entidades JPA.
- `repository`: interfaces Spring Data JPA.
- `service`: logica de negocio y transacciones declarativas con `@Transactional`.
- `security`: JWT, filtro de autenticacion y respuesta JSON para errores 401.
- `exception`: excepciones de dominio y `GlobalExceptionHandler`.

## Decisiones tecnicas

- Persistencia con Spring Data JPA, sin JDBC manual en servicios.
- DTOs separados para Request y Response.
- Inyeccion de dependencias por constructor.
- `MovimientoFactory` centraliza la creacion de movimientos.
- Strategy para generacion de numero de cuenta.
- Usuario en memoria para el modulo: `admin/password`.
- No se incluye entidad `Usuario`, `UsuarioRepository` ni sistema de roles persistido.
- No se usa `DatabaseConnectionManager`, `RepositoryFactory`, `Connection`, `PreparedStatement`, `ResultSet`, `commit` ni `rollback` en el flujo principal.
- Las fechas de creacion se asignan desde JPA/aplicacion mediante callbacks `@PrePersist`. Los `DEFAULT CURRENT_TIMESTAMP` del esquema quedan como respaldo de base de datos.
- Los numeros de cuenta siguen el formato funcional `ES91210000` + 12 digitos secuenciales. La estrategia actual es simple y queda como mejora futura sustituirla por una secuencia de base de datos para concurrencia alta.
- Las relaciones JPA no usan `CascadeType.ALL` ni `orphanRemoval` para evitar borrados accidentales de historico financiero.

## Tecnologias

- Java 17
- Maven
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- JWT con JJWT
- PostgreSQL
- H2 para tests
- Bean Validation
- springdoc-openapi
- JUnit 5, Mockito y MockMvc
- Lombok

## Configuracion

La aplicacion usa PostgreSQL por defecto. Puede configurarse con variables de entorno:

```powershell
$env:NOVABANK_DB_URL="jdbc:postgresql://localhost:5432/novabank"
$env:NOVABANK_DB_USER="postgres"
$env:NOVABANK_DB_PASSWORD="tu_password"
$env:JWT_SECRET="novabank-secret-key-for-jwt-generation-2026"
$env:JWT_EXPIRATION="3600000"
```

Valores por defecto principales (ver `src/main/resources/application.yml`):

- `NOVABANK_DB_URL`: `jdbc:postgresql://localhost:5432/novabank`
- `NOVABANK_DB_USER`: `postgres`
- `NOVABANK_DB_PASSWORD`: `usuario`
- `JWT_EXPIRATION`: `86400000`

Si la base de datos se crea manualmente, puede usarse como referencia:

```text
docs/sql/create-database.sql
```

La base de datos del modulo se llama `novabank`.

## Preparar PostgreSQL

La aplicacion no crea automaticamente la base ni las tablas. La configuracion productiva usa `spring.sql.init.mode: never` y `spring.jpa.hibernate.ddl-auto: validate`, por lo que Spring solo valida que el esquema existe y coincide con las entidades.

Orden recomendado:

1. Crear la base de datos con `docs/sql/create-database.sql`.
2. Ejecutar `src/main/resources/schema.sql` sobre la base `novabank`.
3. Configurar las variables de entorno de conexion.
4. Arrancar la aplicacion.

`src/main/resources/schema.sql` contiene solo estructura de tablas, constraints e indices. No incluye `CREATE DATABASE` ni comandos `psql`, y queda como script de referencia/creacion manual.

## Compilar y probar

```bash
mvn clean compile
mvn clean test
```

Los tests usan H2 en perfil `test` y cubren repositorios, servicios, controladores, seguridad e integracion end-to-end.

## Ejecutar

```bash
mvn spring-boot:run
```

Para ejecutar con H2 usando el perfil de test:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test -Dspring-boot.run.useTestClasspath=true
```

## Swagger

Con la aplicacion levantada:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Los endpoints de Swagger y autenticacion son publicos. El resto requiere token JWT.

## Autenticacion

Login:

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

La respuesta devuelve:

```json
{
  "token": "...",
  "tipo": "Bearer",
  "expiracion": 3600000
}
```

Usar el token en endpoints protegidos:

```http
Authorization: Bearer <token>
```

## Endpoints principales

| Metodo | Endpoint | Acceso | Descripcion |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | Publico | Genera token JWT |
| GET | `/api/clientes` | Protegido | Lista clientes |
| POST | `/api/clientes` | Protegido | Crea cliente |
| GET | `/api/clientes/{id}` | Protegido | Obtiene cliente |
| GET | `/api/clientes?dni=...` | Protegido | Obtiene cliente por DNI |
| POST | `/api/cuentas` | Protegido | Crea cuenta |
| GET | `/api/cuentas/{id}` | Protegido | Obtiene cuenta |
| GET | `/api/cuentas/numero/{numeroCuenta}` | Protegido | Obtiene cuenta por número |
| GET | `/api/cuentas/{id}/saldo` | Protegido | Consulta saldo de cuenta |
| GET | `/api/clientes/{clienteId}/cuentas` | Protegido | Lista cuentas de cliente |
| POST | `/api/operaciones/deposito` | Protegido | Registra deposito |
| POST | `/api/operaciones/retiro` | Protegido | Registra retirada |
| POST | `/api/operaciones/transferencia` | Protegido | Registra transferencia |
| GET | `/api/cuentas/{id}/movimientos` | Protegido | Lista movimientos de cuenta |
| GET | `/api/cuentas/{id}/movimientos?fechaInicio=YYYY-MM-DD&fechaFin=YYYY-MM-DD` | Protegido | Lista movimientos por rango |

## Errores API

`GlobalExceptionHandler` centraliza las respuestas de error con un cuerpo consistente:

```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "Cuenta no encontrada",
  "timestamp": "2026-04-26T12:00:00"
}
```

Para validaciones se incluye `fieldErrors`.

Codigos principales:

- `404`: recurso no encontrado.
- `422`: saldo insuficiente.
- `409`: DNI, email o telefono duplicado.
- `400`: validaciones o datos invalidos.
- `401`: credenciales invalidas o token ausente/invalido.
- `500`: error inesperado.

## Postman

La coleccion esta en:

```text
docs/postman/NovaBank-Modulo-3.postman_collection.json
```

Flujo cubierto: login, clientes (incluye negativos: DNI inválido y duplicados), cuentas (incluye consulta por número y saldo), operaciones y movimientos (incluye rango de fechas).

Nota: recomendado ejecutar sobre base de datos limpia porque DNI/email/teléfono son únicos.

## Entrega limpia

Antes de generar un ZIP de entrega:

```bash
mvn clean
git archive --format=zip --output NovaBank-Modulo3.zip HEAD
```

`git archive` genera una entrega de codigo fuente sin incluir `.git/` ni `target/`.

## Repositorio

https://github.com/CroquetaConQueso/NovaBank

## Autor

Carlos Torres Leon
