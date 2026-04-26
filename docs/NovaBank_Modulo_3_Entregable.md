# NovaBank Digital Services - Entregable Modulo 3

## 1. Objetivo del modulo

El Modulo 3 transforma NovaBank desde una aplicacion de consola con JDBC manual a un backend REST profesional accesible por HTTP.

La solucion implementa:

- API REST con Spring Boot.
- Persistencia con Spring Data JPA.
- DTOs separados entre Request y Response.
- Servicios transaccionales con `@Service` y `@Transactional`.
- Autenticacion con Spring Security y JWT.
- Documentacion OpenAPI/Swagger.
- Testing por capas.
- Limpieza del codigo legacy de consola y JDBC manual.

## 2. Arquitectura final

Paquete base:

```text
com.novabank
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

Responsabilidades:

- `controller`: expone los endpoints REST.
- `dto`: define contratos publicos de entrada y salida.
- `mapper`: convierte entidades JPA a DTOs y viceversa.
- `model`: contiene entidades JPA.
- `repository`: usa Spring Data JPA.
- `service`: concentra reglas de negocio y transacciones.
- `security`: contiene JWT, filtro y configuracion de Spring Security.
- `exception`: centraliza excepciones y respuestas de error.

## 3. Persistencia y transacciones

La persistencia se realiza mediante repositorios Spring Data JPA:

- `ClienteRepository`
- `CuentaRepository`
- `MovimientoRepository`

Las operaciones de negocio se ejecutan desde servicios transaccionales:

- `ClienteService`
- `CuentaService`
- `OperacionService`

No hay uso de JDBC manual en el flujo principal. Se eliminaron las piezas antiguas del Modulo 2 como `DatabaseConnectionManager`, `RepositoryFactory`, conexiones manuales, `PreparedStatement`, `ResultSet`, `commit` y `rollback`.

## 4. DTOs y mappers

Los RequestDTO reciben datos de entrada y aplican validaciones con `jakarta.validation`.

Los ResponseDTO definen lo que se expone al cliente HTTP.

DTOs principales:

- `ClienteRequestDTO`
- `ClienteResponseDTO`
- `CuentaCreateRequestDTO`
- `CuentaResponseDTO`
- `OperacionRequestDTO`
- `TransferenciaRequestDTO`
- `MovimientoResponseDTO`
- `LoginRequestDTO`
- `LoginResponseDTO`
- `ErrorResponseDTO`

Los mappers son manuales y no acceden a repositorios.

## 5. Seguridad

La autenticacion se implementa con Spring Security y JWT.

Componentes:

- `SecurityConfig`
- `JwtService`
- `JwtFilter`
- `JsonAuthenticationEntryPoint`
- `AuthController`

Decisiones:

- Usuario en memoria: `admin/password`.
- PasswordEncoder con BCrypt.
- `/api/auth/**` y Swagger son publicos.
- El resto de endpoints requiere token JWT.
- No se crea entidad `Usuario` ni repositorio de usuarios en este modulo.

## 6. Manejo global de errores

`GlobalExceptionHandler` usa `@RestControllerAdvice` y metodos `@ExceptionHandler` para evitar try/catch en controladores.

Errores cubiertos:

- `ResourceNotFoundException`, `ClienteNotFoundException`, `CuentaNotFoundException`: 404.
- `InsufficientBalanceException`, `SaldoInsuficienteException`: 422.
- `IllegalArgumentException`: 400.
- `MethodArgumentNotValidException`: 400 con `fieldErrors`.
- `AuthenticationException`: 401.
- `Exception`: 500.

Las respuestas usan `ErrorResponseDTO` con:

- `code`
- `message`
- `timestamp`
- `fieldErrors`, cuando aplica.

## 7. Endpoints principales

- `POST /api/auth/login`
- `GET /api/clientes`
- `POST /api/clientes`
- `GET /api/clientes/{id}`
- `POST /api/cuentas`
- `GET /api/cuentas/{id}`
- `GET /api/clientes/{clienteId}/cuentas`
- `POST /api/operaciones/deposito`
- `POST /api/operaciones/retiro`
- `POST /api/operaciones/transferencia`
- `GET /api/cuentas/{id}/movimientos`

## 8. Testing por capas

La suite incluye:

- Tests de repositorio con `@DataJpaTest`.
- Tests unitarios de servicios con Mockito.
- Tests web con MockMvc.
- Tests de seguridad e integracion con `@SpringBootTest`.
- Flujo end-to-end autenticado.

Validacion final:

```bash
mvn clean test
```

El flujo end-to-end prueba login, cliente, cuenta, deposito, retirada, transferencia, saldo insuficiente y consulta de movimientos.

## 9. Documentacion y herramientas

- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`
- Coleccion Postman: `docs/postman/NovaBank-Modulo-3.postman_collection.json`

## 10. Estado final

El modulo queda preparado como API REST Spring Boot, con el codigo legacy de consola/JDBC eliminado del flujo principal y con validacion automatizada mediante Maven.
