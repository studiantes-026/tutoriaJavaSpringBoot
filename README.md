# Guía de Proyecto: API REST con Spring Boot, JPA, PostgreSQL, JWT y Validaciones

> **Curso:** Java Backend con Spring Boot  
> **Objetivo:** Construir una API REST profesional con arquitectura en capas, PostgreSQL, Spring Security, autenticación **stateless** con JWT, autorización por roles, validación de inputs y manejo global de errores.  
> Compatible con **Windows** y **Linux/Ubuntu/macOS**.

---

# Tabla de contenido

1. [Introducción](#1-introducción)
2. [Mapa del repositorio (carpetas y archivos)](#2-mapa-del-repositorio-carpetas-y-archivos)
3. [Prerrequisitos](#3-prerrequisitos)
4. [Clonar el repositorio y correr el proyecto por primera vez](#4-clonar-el-repositorio-y-correr-el-proyecto-por-primera-vez)
5. [PostgreSQL: con Docker y sin Docker](#5-postgresql-con-docker-y-sin-docker)
6. [Arquitectura y flujo interno](#6-arquitectura-y-flujo-interno)
7. [Capas del dominio: entidades, repositorios, servicios, controladores y DTOs](#7-capas-del-dominio-entidades-repositorios-servicios-controladores-y-dtos)
8. [`application.properties` y `ddl-auto`](#8-applicationproperties-y-ddl-auto)
9. [Rutas HTTP, JWT y roles](#9-rutas-http-jwt-y-roles)
10. [Implementación de JWT y Spring Security (paso a paso)](#10-implementación-de-jwt-y-spring-security-paso-a-paso)
11. [Tabla de métodos de las librerías de seguridad y JWT](#11-tabla-de-métodos-de-las-librerías-de-seguridad-y-jwt)
12. [Módulo 3 — Clase 4: Validaciones, excepciones y APIs profesionales](#12-módulo-3--clase-4-validaciones-excepciones-y-apis-profesionales)
13. [Pruebas paso a paso: terminal y Postman](#13-pruebas-paso-a-paso-terminal-y-postman)
14. [Herramientas visuales y analogía didáctica](#14-herramientas-visuales-y-analogía-didáctica)
15. [Errores comunes](#15-errores-comunes)
16. [Resumen del curso](#16-resumen-del-curso)

---

# 1. Introducción

Durante el curso se construye una API REST donde:

- Un **Usuario** puede tener muchos **Pedidos**.
- Cada **Pedido** pertenece a un único **Usuario**.
- El acceso a la API se protege con **Spring Security** y **JWT** (sin sesiones).
- Los permisos se definen con **roles** (`ROLE_USER`, `ROLE_ADMIN`).
- Los inputs se validan con **Jakarta Validation** y los errores se unifican con `@RestControllerAdvice`.

```text
Usuario (1) ──────── (N) Pedido
   │
   └── rol: ROLE_USER | ROLE_ADMIN
```

Flujo de autenticación:

1. El cliente envía credenciales a `POST /api/auth/login`.
2. Si son válidas, el backend firma un JWT (incluye `sub` y el claim `rol`).
3. El cliente envía el token en `Authorization: Bearer <token>` en cada petición.
4. `JwtAuthenticationFilter` valida el token y registra al usuario (y sus autoridades) en `SecurityContextHolder`.
5. `SecurityConfig` decide si la ruta es pública, exige autenticación o exige un rol.

> Ajusta el paquete `com.example.project` si tu proyecto usa otro.

---

# 2. Mapa del repositorio (carpetas y archivos)

Explicación de **cada carpeta y archivo** que el estudiante encuentra al clonar.

```text
tutoriaJavaSpringBoot/
│
├── .mvn/wrapper/
│     └── maven-wrapper.properties     # Versión de Maven que descarga el wrapper
├── src/
│   ├── main/
│   │   ├── java/com/example/project/
│   │   │     ├── ProjectApplication.java
│   │   │     ├── config/
│   │   │     │     ├── SecurityConfig.java
│   │   │     │     ├── JwtUtils.java
│   │   │     │     └── JwtAuthenticationFilter.java
│   │   │     ├── controller/
│   │   │     │     ├── AuthController.java
│   │   │     │     ├── UsuarioController.java
│   │   │     │     └── PedidoController.java
│   │   │     ├── dto/
│   │   │     │     ├── AuthDTO.java
│   │   │     │     ├── UsuarioDTO.java
│   │   │     │     ├── UsuarioRequestDTO.java
│   │   │     │     ├── PedidoDTO.java
│   │   │     │     └── ErrorResponseDTO.java
│   │   │     ├── entity/
│   │   │     │     ├── Usuario.java
│   │   │     │     └── Pedido.java
│   │   │     ├── exception/
│   │   │     │     └── GlobalExceptionHandler.java
│   │   │     ├── repository/
│   │   │     │     ├── UsuarioRepository.java
│   │   │     │     └── PedidoRepository.java
│   │   │     └── service/
│   │           ├── UsuarioService.java
│   │           └── PedidoService.java
│   │   └── resources/
│   │         └── application.properties
│   └── test/java/com/example/project/
│         └── ProjectApplicationTests.java
│
├── pom.xml
├── mvnw / mvnw.cmd
├── docker-compose.yml
├── .env                  (local; no se sube a Git)
├── .gitignore
├── .gitattributes
└── README.md             (este documento)
```

| Ruta | Para qué sirve |
|------|----------------|
| `pom.xml` | Dependencias Maven: Web, JPA, PostgreSQL, Security, JJWT, Validation, Lombok, tests. Spring Boot **4.1.0**, Java **21**. |
| `mvnw` / `mvnw.cmd` | Wrapper de Maven. Permite compilar **sin instalar Maven** globalmente. |
| `.mvn/wrapper/maven-wrapper.properties` | Indica qué distribución de Maven descarga el wrapper (3.9.16). |
| `docker-compose.yml` | Levanta **PostgreSQL 17** (puerto `5432`) y **pgAdmin** (puerto `5050`). |
| `.env` | Credenciales de Docker (`POSTGRES_*`, `PGADMIN_*`). Está en `.gitignore`; cada estudiante lo crea localmente. |
| `.gitignore` | Ignora `target/`, `.idea/`, `.env`, logs, etc. |
| `ProjectApplication.java` | Punto de entrada (`@SpringBootApplication` + `main`). |
| `entity/` | Tablas JPA: `usuarios` y `pedidos`. |
| `repository/` | Acceso a datos (`JpaRepository`, consultas derivadas y `@Query`). |
| `service/` | Reglas de negocio y transacciones. |
| `controller/` | Endpoints HTTP. |
| `dto/` | Objetos de transferencia (login, respuestas, validación, errores). |
| `config/` | Seguridad: cadena de filtros, JWT y filtro Bearer. |
| `exception/` | Manejador global de errores (`@RestControllerAdvice`). |
| `application.properties` | URL de PostgreSQL, JPA y usuario de prueba de Spring Security. |
| `src/test/.../ProjectApplicationTests.java` | Test de humo: el contexto de Spring arranca. |

**Cómo se relacionan las capas (un request típico):**

```text
HTTP → JwtAuthenticationFilter → SecurityFilterChain
     → Controller → Service → Repository → Hibernate → PostgreSQL
```

---

# 3. Prerrequisitos

- **JDK 21** (el `pom.xml` declara `<java.version>21</java.version>`).
- Git.
- Editor: IntelliJ, VS Code o Eclipse.
- **Maven no es obligatorio** (usar `mvnw` / `mvnw.cmd`).
- **Opción Docker:** Docker Desktop (Windows/macOS) o Docker Engine + Compose (Linux).
- **Opción sin Docker:** PostgreSQL 16/17 instalado en el sistema.
- (Opcional) Postman, navegador, [jwt.io](https://jwt.io/) y [CyberChef](https://gchq.github.io/CyberChef/).

Verificar Java:

```bash
java -version
```

En Windows (CMD o PowerShell) el comando es el mismo.

---

# 4. Clonar el repositorio y correr el proyecto por primera vez

Ejecuta los comandos **desde la raíz del proyecto** (donde está `pom.xml`).

## 4.1 Clonar

```bash
git clone <URL_DEL_REPOSITORIO>
cd tutoriaJavaSpringBoot
```

## 4.2 Crear el archivo `.env`

`.env` no viaja en Git. Crea uno en la raíz:

**Windows (PowerShell):**

```powershell
@"
POSTGRES_USER=postgres
POSTGRES_PASSWORD=123456
POSTGRES_DB=projectdb
PGADMIN_DEFAULT_EMAIL=admin@admin.com
PGADMIN_DEFAULT_PASSWORD=123456
"@ | Set-Content -Encoding utf8 .env
```

**Linux/Ubuntu/macOS:**

```bash
cat > .env << 'EOF'
POSTGRES_USER=postgres
POSTGRES_PASSWORD=123456
POSTGRES_DB=projectdb
PGADMIN_DEFAULT_EMAIL=admin@admin.com
PGADMIN_DEFAULT_PASSWORD=123456
EOF
```

Esos valores deben coincidir con `application.properties` (`projectdb`, usuario `postgres`, password `123456`).

## 4.3 Permiso del wrapper (solo Linux/macOS, una vez)

```bash
chmod +x mvnw
```

## 4.4 Levantar PostgreSQL

Elige **una** de las dos opciones de la [sección 5](#5-postgresql-con-docker-y-sin-docker). Con Docker:

```bash
docker compose up -d
```

Comprobar que el contenedor está sano:

```bash
docker compose ps
```

## 4.5 Compilar, testear y arrancar

**Windows (CMD o PowerShell):**

```cmd
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

**Linux/Ubuntu/macOS:**

```bash
./mvnw clean install
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080`. Deja esa terminal abierta. Las pruebas se hacen en **otra** terminal o en Postman ([sección 13](#13-pruebas-paso-a-paso-terminal-y-postman)).

Otros comandos útiles:

| Acción | Windows | Linux/macOS |
|--------|---------|-------------|
| Solo tests | `mvnw.cmd test` | `./mvnw test` |
| Solo compilar | `mvnw.cmd compile` | `./mvnw compile` |
| Descargar dependencias | `mvnw.cmd dependency:resolve` | `./mvnw dependency:resolve` |

> Si el proyecto usara Gradle: `gradlew.bat bootRun` / `./gradlew bootRun`. Este repositorio usa **Maven**.

---

# 5. PostgreSQL: con Docker y sin Docker

La aplicación se conecta siempre a `localhost:5432/projectdb`. Lo único que cambia es **quién** escucha ese puerto.

## 5.1 Con Docker (recomendado en clase)

Archivo `docker-compose.yml`:

- Servicio `postgres`: imagen `postgres:17`, puerto `5432:5432`, volumen `postgres_data`, healthcheck `pg_isready`.
- Servicio `pgadmin`: interfaz web en `http://localhost:5050`.

**Comandos (Windows PowerShell, CMD y Linux — iguales):**

```bash
docker compose up -d
docker compose ps
docker compose logs -f postgres
docker compose down
```

Borrar también el volumen (borra los datos de la BD):

```bash
docker compose down -v
```

Entrar a `psql` dentro del contenedor:

```bash
docker exec -it postgres17 psql -U postgres -d projectdb
```

Dentro de `psql`:

```sql
\dt
SELECT * FROM usuarios;
\q
```

**pgAdmin:** abre `http://localhost:5050`, inicia sesión con `PGADMIN_DEFAULT_EMAIL` / `PGADMIN_DEFAULT_PASSWORD`. Registra un servidor:

- Host: `postgres` (nombre del servicio en Docker) o `host.docker.internal` según tu red.
- Puerto: `5432`
- Usuario / contraseña: los de `.env`

## 5.2 Sin Docker (PostgreSQL instalado en el SO)

1. Instala PostgreSQL 16 o 17 desde el instalador oficial (Windows) o el paquete del sistema (Linux).
2. Durante la instalación anota el usuario (`postgres`) y la contraseña.
3. Crea la base:

**Windows** (SQL Shell `psql` o pgAdmin Query Tool):

```sql
CREATE DATABASE projectdb;
```

**Linux/Ubuntu:**

```bash
sudo -u postgres psql
```

```sql
CREATE DATABASE projectdb;
\q
```

4. Ajusta `spring.datasource.password` en `application.properties` si tu contraseña local no es `123456`.
5. Arranca el servicio de PostgreSQL si no está activo:

**Windows (PowerShell como administrador, ejemplo de servicio):**

```powershell
Get-Service -Name "*postgres*"
# Start-Service postgresql-x64-17
```

**Linux (systemd):**

```bash
sudo systemctl start postgresql
sudo systemctl status postgresql
```

No ejecutes Docker **y** PostgreSQL local al mismo tiempo en el puerto `5432`: uno de los dos fallará por el puerto ocupado.

---

# 6. Arquitectura y flujo interno

Arquitectura en capas: cada paquete tiene una responsabilidad.

```text
Cliente (Postman / curl / frontend)
        │
        ▼
Apache Tomcat (embebido en Spring Boot)
        │
        ▼
JwtAuthenticationFilter  →  SecurityFilterChain
        │
        ▼
Controller (@RestController)
        │
        ▼
Service (@Service, @Transactional)
        │
        ▼
Repository (JpaRepository)
        │
        ▼
Hibernate (ORM)
        │
        ▼
PostgreSQL
```

| Capa | Responsabilidad |
|------|-----------------|
| Filtro JWT | Lee `Authorization`, valida firma y carga roles |
| SecurityConfig | Rutas públicas vs autenticadas vs por rol |
| Controller | Recibe HTTP y responde JSON |
| Service | Lógica de negocio |
| Repository | Acceso a datos |
| Hibernate | Objetos Java ↔ SQL |
| PostgreSQL | Persistencia |

---

# 7. Capas del dominio: entidades, repositorios, servicios, controladores y DTOs

## 7.1 Entidades

Representan tablas. `Usuario` → `usuarios`. `Pedido` → `pedidos`.

Anotaciones usadas:

| Anotación | Uso |
|-----------|-----|
| `@Entity` | Clase Java = tabla |
| `@Table(name = "...")` | Nombre de tabla |
| `@Id` + `@GeneratedValue(IDENTITY)` | PK autogenerada (`BIGSERIAL` / `IDENTITY`) |
| `@Column` | `nullable`, `unique`, `length`, `precision` |
| `@OneToMany(mappedBy = "usuario", cascade = ALL, orphanRemoval = true)` | Un usuario, muchos pedidos |
| `@ManyToOne(fetch = LAZY)` + `@JoinColumn(name = "usuario_id")` | FK `usuario_id` |

Campo de seguridad en `Usuario`:

```java
@Column(name = "rol", nullable = false, length = 50)
private String rol; // Ej: ROLE_USER, ROLE_ADMIN
```

Tipos nativos frecuentes: `Long`, `String`, `BigDecimal` (dinero), `LocalDateTime`, `List`, `Optional`.

## 7.2 Repositorios

`UsuarioRepository` y `PedidoRepository` extienden `JpaRepository<Entidad, Long>` y heredan `save`, `findAll`, `findById`, `deleteById`, `existsById`, `count`.

Consultas derivadas (Spring arma el SQL por el nombre):

```java
Optional<Usuario> findByEmail(String email);
boolean existsByEmail(String email);
List<Pedido> findByUsuarioId(Long usuarioId);
```

JPQL (`PedidoRepository`):

```java
@Query("SELECT p FROM Pedido p WHERE p.total > :minimo")
List<Pedido> buscarPedidosMayoresA(@Param("minimo") BigDecimal minimo);

@Query("SELECT p FROM Pedido p JOIN FETCH p.usuario WHERE p.usuario.id = :usuarioId")
List<Pedido> buscarPedidosConUsuarioPorId(@Param("usuarioId") Long usuarioId);
```

JPQL consulta **entidades**, no tablas. SQL nativo usaría `nativeQuery = true`.

## 7.3 Servicios

`@Service` + inyección por constructor. `@Transactional` hace `COMMIT`/`ROLLBACK`. `@Transactional(readOnly = true)` para lecturas.

Regla de negocio en `UsuarioService.guardar`: si `existsByEmail`, lanza `IllegalArgumentException` (luego la captura el manejador global).

## 7.4 Controladores

| Clase | Prefijo | Rol |
|-------|---------|-----|
| `AuthController` | `/api/auth` | Login y emisión de JWT |
| `UsuarioController` | `/api/usuarios` | CRUD de usuarios |
| `PedidoController` | `/api/pedidos` | Pedidos y filtro por total |

Anotaciones HTTP: `@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@DeleteMapping`, `@RequestBody`, `@PathVariable`, `@RequestParam`, `ResponseEntity`.

## 7.5 DTOs

Evitan ciclos JSON (`Usuario` ↔ `Pedido`) y exponen solo lo necesario.

| DTO | Uso |
|-----|-----|
| `UsuarioDTO` | Respuesta ligera de usuario |
| `PedidoDTO` | Pedido + `usuarioId` y `usuarioNombre` (sin anidar la entidad) |
| `AuthDTO` | `LoginRequest` y `TokenResponse` |
| `UsuarioRequestDTO` | Alta de usuario con `@NotBlank`, `@Email`, `@Size` |
| `ErrorResponseDTO` | Formato único de error JSON |

---

# 8. `application.properties` y `ddl-auto`

Archivo: `src/main/resources/application.properties`.

```properties
spring.application.name=project

spring.datasource.url=jdbc:postgresql://localhost:5432/projectdb
spring.datasource.username=postgres
spring.datasource.password=123456
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Usuario en memoria de Spring Security (pruebas iniciales; el login JWT de clase usa AuthController)
spring.security.user.name=admin
spring.security.user.password=admin123
```

`spring.jpa.hibernate.ddl-auto=update` compara entidades con PostgreSQL y crea/actualiza tablas y FKs. No sustituye migraciones (Flyway/Liquibase) en producción.

---

# 9. Rutas HTTP, JWT y roles

## 9.1 Tabla de endpoints

| Método | Ruta | Auth | Roles | Descripción |
|--------|------|------|-------|-------------|
| `POST` | `/api/auth/login` | Pública | — | Devuelve JWT |
| `POST` | `/api/usuarios` | Pública | — | Registrar usuario |
| `GET` | `/api/usuarios` | JWT | `ADMIN` | Listar usuarios |
| `GET` | `/api/usuarios/{id}` | JWT | autenticado | Buscar por id |
| `DELETE` | `/api/usuarios/{id}` | JWT | `ADMIN` | Eliminar usuario |
| `GET` | `/api/pedidos` | JWT | `USER` o `ADMIN` | Listar pedidos |
| `GET` | `/api/pedidos/usuario/{usuarioId}` | JWT | `USER` o `ADMIN` | Pedidos de un usuario |
| `POST` | `/api/pedidos/usuario/{usuarioId}` | JWT | `USER` o `ADMIN` | Crear pedido |
| `GET` | `/api/pedidos/filtrar?minimo=1000` | JWT | `USER` o `ADMIN` | Pedidos con total mayor a `minimo` |

`hasRole("ADMIN")` exige la autoridad `ROLE_ADMIN`. Por eso el claim y la columna `rol` deben guardar el prefijo `ROLE_`.

## 9.2 Flujo del token con roles (lo implementado en clase)

1. Al generar el JWT se agrega un **claim personalizado** `rol`.
2. El filtro lee `obtenerRolDelToken` y crea `SimpleGrantedAuthority`.
3. `SecurityContextHolder` queda con usuario **y** autoridades.
4. `authorizeHttpRequests` aplica `hasRole` / `hasAnyRole`.

```text
Login → generarToken(username, rol)
     → Header.Payload.Signature
     → Filtro: validar + authorities
     → hasRole("ADMIN") / hasAnyRole("USER","ADMIN")
```

---

# 10. Implementación de JWT y Spring Security (paso a paso)

## Paso 0 — Dependencia JJWT

En `pom.xml`, dentro de `<dependencies>`:

```xml
<!-- JJWT: creación y validación de JSON Web Tokens -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

También debe existir `spring-boot-starter-security`.

Descargar dependencias:

**Windows:**

```powershell
.\mvnw.cmd clean dependency:resolve
```

**Linux/macOS:**

```bash
./mvnw clean dependency:resolve
```

> Versión `0.11.5`: API `Jwts.parserBuilder()`, `.signWith(SECRET_KEY)`. JJWT `0.12+` cambia esa sintaxis.

## Paso 1 — Paquetes

**Windows (PowerShell):**

```powershell
New-Item -ItemType Directory -Force -Path "src\main\java\com\example\project\config"
New-Item -ItemType Directory -Force -Path "src\main\java\com\example\project\dto"
New-Item -ItemType Directory -Force -Path "src\main\java\com\example\project\controller"
New-Item -ItemType Directory -Force -Path "src\main\java\com\example\project\exception"
```

**Linux/macOS:**

```bash
mkdir -p src/main/java/com/example/project/{config,dto,controller,exception}
```

## Paso 2 — `JwtUtils.java`

📁 `src/main/java/com/example/project/config/JwtUtils.java`

Encapsula firmar, leer claims y validar. El claim `rol` viaja en el payload.

```java
package com.example.project.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // En producción: leer de application.properties (no hardcodear ni regenerar en cada arranque)
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME = 3600000; // 1 hora

    public String generarToken(String username, String rol) {
        return Jwts.builder()
                .setSubject(username)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    public String obtenerUsernameDelToken(String token) {
        return obtenerClaims(token).getSubject();
    }

    public String obtenerRolDelToken(String token) {
        return obtenerClaims(token).get("rol", String.class);
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validarToken(String token) {
        try {
            obtenerClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

## Paso 3 — `JwtAuthenticationFilter.java`

📁 `src/main/java/com/example/project/config/JwtAuthenticationFilter.java`

`OncePerRequestFilter`: una ejecución por petición. Si hay `Bearer` válido, autentica **con roles**.

```java
package com.example.project.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validarToken(token)) {
                String username = jwtUtils.obtenerUsernameDelToken(token);
                String rol = jwtUtils.obtenerRolDelToken(token);
                List<SimpleGrantedAuthority> authorities =
                        Collections.singletonList(new SimpleGrantedAuthority(rol));
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

## Paso 4 — `AuthDTO.java` y `AuthController.java`

📁 `src/main/java/com/example/project/dto/AuthDTO.java`

```java
package com.example.project.dto;

public class AuthDTO {

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class TokenResponse {
        private String token;

        public TokenResponse(String token) { this.token = token; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}
```

📁 `src/main/java/com/example/project/controller/AuthController.java`

Versión didáctica de clase: credenciales fijas. El **rol** se empaqueta en el token para poder probar `ADMIN` vs `USER`.

```java
package com.example.project.controller;

import com.example.project.config.JwtUtils;
import com.example.project.dto.AuthDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtils jwtUtils;

    public AuthController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDTO.LoginRequest request) {
        if ("admin".equals(request.getUsername()) && "admin123".equals(request.getPassword())) {
            String token = jwtUtils.generarToken(request.getUsername(), "ROLE_ADMIN");
            return ResponseEntity.ok(new AuthDTO.TokenResponse(token));
        }
        if ("user".equals(request.getUsername()) && "user123".equals(request.getPassword())) {
            String token = jwtUtils.generarToken(request.getUsername(), "ROLE_USER");
            return ResponseEntity.ok(new AuthDTO.TokenResponse(token));
        }
        return ResponseEntity.status(401).body("Credenciales incorrectas.");
    }
}
```

| Usuario de práctica | Contraseña | Rol en el JWT |
|---------------------|------------|----------------|
| `admin` | `admin123` | `ROLE_ADMIN` |
| `user` | `user123` | `ROLE_USER` |

Siguiente evolución natural: consultar PostgreSQL + `PasswordEncoder` (BCrypt) en lugar de credenciales fijas.

## Paso 5 — `SecurityConfig.java`

📁 `src/main/java/com/example/project/config/SecurityConfig.java`

```java
package com.example.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/usuarios").hasRole("ADMIN")
                    .requestMatchers("/api/pedidos/**").hasAnyRole("USER", "ADMIN")
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

- **CSRF off:** API stateless con token en header, no cookies de sesión.
- **STATELESS:** no `JSESSIONID`.
- **`@EnableMethodSecurity`:** habilita `@PreAuthorize` en métodos si se necesita un segundo nivel de control.

Tras estos pasos, compila y arranca con los comandos de la [sección 4.5](#45-compilar-testear-y-arrancar).

---

# 11. Tabla de métodos de las librerías de seguridad y JWT

## 11.1 JJWT (`io.jsonwebtoken`)

| Método / API | Clase | Qué hace en este proyecto |
|--------------|-------|---------------------------|
| `Keys.secretKeyFor(SignatureAlgorithm.HS256)` | `Keys` | Genera clave HMAC-SHA256 (≥ 256 bits). Aquí se recrea en cada arranque (tokens viejos dejan de valer). |
| `Jwts.builder()` | `Jwts` | Inicia la construcción del token. |
| `.setSubject(username)` | `JwtBuilder` | Claim `sub`: identidad. |
| `.claim("rol", rol)` | `JwtBuilder` | Claim personalizado con `ROLE_USER` / `ROLE_ADMIN`. |
| `.setIssuedAt(Date)` | `JwtBuilder` | Claim `iat`. |
| `.setExpiration(Date)` | `JwtBuilder` | Claim `exp` (1 hora). |
| `.signWith(SECRET_KEY)` | `JwtBuilder` | Firma HS256. |
| `.compact()` | `JwtBuilder` | Cadena `header.payload.signature` en Base64Url. |
| `Jwts.parserBuilder()` | `Jwts` | Parser 0.11.x. |
| `.setSigningKey(SECRET_KEY)` | `JwtParserBuilder` | Clave para verificar firma. |
| `.build()` | `JwtParserBuilder` | Instancia inmutable del parser. |
| `.parseClaimsJws(token)` | `JwtParser` | Verifica firma y `exp`. Lanza si está manipulado o vencido. |
| `.getBody()` | `Jws<Claims>` | Payload (`Claims`). |
| `Claims.getSubject()` | `Claims` | Lee `sub`. |
| `Claims.get("rol", String.class)` | `Claims` | Lee el rol. |

## 11.2 Spring Security — configuración y filtro

| Método / API | Dónde | Qué hace |
|--------------|-------|----------|
| `@EnableWebSecurity` | `SecurityConfig` | Activa el modelo de seguridad web. |
| `@EnableMethodSecurity` | `SecurityConfig` | Permite `@PreAuthorize`, `@Secured`. |
| `@Bean SecurityFilterChain` | `SecurityConfig` | Cadena de filtros HTTP. |
| `csrf(csrf -> csrf.disable())` | `HttpSecurity` | Desactiva CSRF (API Bearer). |
| `sessionCreationPolicy(STATELESS)` | `SessionManagement` | Sin sesión servidor. |
| `authorizeHttpRequests(...)` | `HttpSecurity` | Reglas por ruta. |
| `requestMatchers(...).permitAll()` | `AuthorizeHttpRequests` | Público (`/api/auth/**`, `POST /api/usuarios`). |
| `hasRole("ADMIN")` | Idem | Exige autoridad `ROLE_ADMIN`. |
| `hasAnyRole("USER", "ADMIN")` | Idem | Pedidos. |
| `anyRequest().authenticated()` | Idem | El resto necesita JWT válido. |
| `addFilterBefore(filtro, UsernamePasswordAuthenticationFilter.class)` | `HttpSecurity` | JWT **antes** del filtro de usuario/clave. |
| `http.build()` | `HttpSecurity` | Construye la cadena. |
| `OncePerRequestFilter.doFilterInternal` | Filtro JWT | Un pase por request. |
| `request.getHeader("Authorization")` | Servlet | Lee el Bearer. |
| `substring(7)` | `String` | Quita el prefijo `"Bearer "`. |
| `new SimpleGrantedAuthority(rol)` | Spring Security | Autoridad que entiende `hasRole`. |
| `UsernamePasswordAuthenticationToken(principal, creds, authorities)` | Spring Security | Objeto `Authentication`. |
| `SecurityContextHolder.getContext().setAuthentication(...)` | Spring Security | Marca el hilo como autenticado. |
| `filterChain.doFilter(...)` | Servlet | Sigue la cadena. |

## 11.3 Spring Security — controladores (anotaciones útiles)

| Anotación / método | Uso |
|--------------------|-----|
| `@PreAuthorize("hasRole('ADMIN')")` | Extra sobre un método (requiere `@EnableMethodSecurity`). |
| `@PreAuthorize("hasAnyRole('USER','ADMIN')")` | Varios roles a nivel método. |
| `Authentication` inyectado | Identidad actual (`getName()`, `getAuthorities()`). |

## 11.4 Jakarta Validation y errores (Clase 4)

| API | Uso |
|-----|-----|
| `@NotBlank` | No nulo, no vacío, no solo espacios. |
| `@Size(min, max)` | Longitud de texto. |
| `@Email` | Formato de correo. |
| `@NotNull` / `@Min` | Números y obligatoriedad. |
| `@Valid` | Dispara validación del `@RequestBody`. |
| `@RestControllerAdvice` | Interceptor global de excepciones. |
| `@ExceptionHandler(Clase.class)` | Mapea una excepción a JSON + status. |
| `MethodArgumentNotValidException` | Fallo de `@Valid`. |
| `FieldError.getField()` / `getDefaultMessage()` | Detalle campo a campo. |

---

# 12. Módulo 3 — Clase 4: Validaciones, excepciones y APIs profesionales

Última clase del temario: validación automática de inputs HTTP y errores JSON uniformes.

## 12.1 Instalar `spring-boot-starter-validation`

**Linux/Ubuntu (Bash), en la raíz del proyecto:**

```bash
sed -i '/<\/dependencies>/i \        <!-- Dependencia para Validaciones Jakarta Validation -->\n        <dependency>\n            <groupId>org.springframework.boot<\/groupId>\n            <artifactId>spring-boot-starter-validation<\/artifactId>\n        <\/dependency>' pom.xml

./mvnw clean dependency:resolve
```

**Windows (PowerShell):**

```powershell
(Get-Content pom.xml) -replace '</dependencies>', "    <!-- Dependencia para Validaciones Jakarta Validation -->`n        <dependency>`n            <groupId>org.springframework.boot</groupId>`n            <artifactId>spring-boot-starter-validation</artifactId>`n        </dependency>`n    </dependencies>" | Set-Content pom.xml; .\mvnw.cmd clean dependency:resolve
```

Si `sed`/`-replace` duplica el cierre, pega a mano la dependencia **una sola vez** dentro de `<dependencies>`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

## 12.2 `UsuarioRequestDTO.java`

📁 `src/main/java/com/example/project/dto/UsuarioRequestDTO.java`

```java
package com.example.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UsuarioRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe proporcionar una dirección de correo electrónico válida")
    private String email;

    @NotBlank(message = "El rol es obligatorio")
    private String rol;

    public UsuarioRequestDTO() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
```

## 12.3 `ErrorResponseDTO.java`

📁 `src/main/java/com/example/project/dto/ErrorResponseDTO.java`

```java
package com.example.project.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponseDTO {

    private int codigoEstado;
    private String mensaje;
    private LocalDateTime timestamp;
    private Map<String, String> detalles;

    public ErrorResponseDTO(int codigoEstado, String mensaje, Map<String, String> detalles) {
        this.codigoEstado = codigoEstado;
        this.mensaje = mensaje;
        this.detalles = detalles;
        this.timestamp = LocalDateTime.now();
    }

    public int getCodigoEstado() { return codigoEstado; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, String> getDetalles() { return detalles; }
}
```

## 12.4 `GlobalExceptionHandler.java`

📁 `src/main/java/com/example/project/exception/GlobalExceptionHandler.java`

```java
package com.example.project.exception;

import com.example.project.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        ErrorResponseDTO respuesta = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Falla en la validación de los datos de entrada",
                errores
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> manejarReglasNegocio(IllegalArgumentException ex) {
        ErrorResponseDTO respuesta = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> manejarExcepcionesGenerales(Exception ex) {
        ErrorResponseDTO respuesta = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocurrió un error interno en el servidor: " + ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
    }
}
```

## 12.5 Activar `@Valid` en `UsuarioController`

Al poner `@Valid` antes de `@RequestBody`, Spring valida el DTO **antes** de entrar al método. El `try/catch` de `IllegalArgumentException` en el controller deja de ser necesario: lo cubre el advice.

Fragmento relevante del `POST`:

```java
@PostMapping
public ResponseEntity<?> crear(@Valid @RequestBody UsuarioRequestDTO dto) {
    Usuario usuario = new Usuario(dto.getNombre(), dto.getEmail(), dto.getRol());
    Usuario nuevoUsuario = usuarioService.guardar(usuario);
    return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
}
```

Import: `jakarta.validation.Valid`.

Los `GET`/`DELETE` existentes se mantienen; solo cambia el contrato de creación.

---

# 13. Pruebas paso a paso: terminal y Postman

Arranca la app **una vez** ([sección 4.5](#45-compilar-testear-y-arrancar)) y deja esa terminal corriendo.

> **Windows PowerShell:** `curl` es un alias de `Invoke-WebRequest`. Usa **`curl.exe`** o ejecuta los mismos comandos en **CMD**. En Linux/macOS usa `curl`.

## 13.1 Terminal — seguridad y JWT

### A. Recurso protegido sin token → 401

**Linux/macOS:**

```bash
curl -i -X GET http://localhost:8080/api/usuarios
```

**Windows CMD:**

```cmd
curl -i -X GET http://localhost:8080/api/usuarios
```

**Windows PowerShell:**

```powershell
curl.exe -i -X GET http://localhost:8080/api/usuarios
```

Esperado: `401 Unauthorized`.

### B. Login ADMIN

**Linux/macOS:**

```bash
curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "admin", "password": "admin123"}'
```

**Windows CMD (una línea):**

```cmd
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\": \"admin\", \"password\": \"admin123\"}"
```

**Windows PowerShell:**

```powershell
curl.exe -X POST http://localhost:8080/api/auth/login `
     -H "Content-Type: application/json" `
     -d "{\"username\": \"admin\", \"password\": \"admin123\"}"
```

Esperado: `{"token":"eyJ..."}`. Copia el token.

**PowerShell nativo (guarda el token en variable):**

```powershell
$loginAdmin = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
$tokenAdmin = $loginAdmin.token
$tokenAdmin
```

### C. Listar usuarios con token ADMIN → 200

**Linux/macOS:**

```bash
curl -i -X GET http://localhost:8080/api/usuarios \
     -H "Authorization: Bearer PEGA_AQUI_EL_TOKEN"
```

**Windows PowerShell:**

```powershell
curl.exe -i -X GET http://localhost:8080/api/usuarios -H "Authorization: Bearer $tokenAdmin"
```

### D. Login USER y listar usuarios → 403

```bash
curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "user", "password": "user123"}'
```

Luego `GET /api/usuarios` con ese token. Esperado: **`403 Forbidden`**.

PowerShell:

```powershell
$loginUser = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"user","password":"user123"}'
$tokenUser = $loginUser.token
curl.exe -i -X GET http://localhost:8080/api/usuarios -H "Authorization: Bearer $tokenUser"
```

### E. Pedidos con USER o ADMIN → 200 (si hay datos)

```bash
curl -i -X GET http://localhost:8080/api/pedidos \
     -H "Authorization: Bearer PEGA_TOKEN_USER_O_ADMIN"
```

## 13.2 Terminal — validaciones (Clase 4)

### Crear usuario inválido (sin token: `POST` es público) → 400

**Linux/macOS:**

```bash
curl -i -X POST http://localhost:8080/api/usuarios \
     -H "Content-Type: application/json" \
     -d '{"nombre": "A", "email": "correo-invalido", "rol": ""}'
```

**Windows CMD:**

```cmd
curl -i -X POST http://localhost:8080/api/usuarios -H "Content-Type: application/json" -d "{\"nombre\": \"A\", \"email\": \"correo-invalido\", \"rol\": \"\"}"
```

Cuerpo esperado:

```json
{
  "codigoEstado": 400,
  "mensaje": "Falla en la validación de los datos de entrada",
  "timestamp": "2026-09-10T00:00:00",
  "detalles": {
    "nombre": "El nombre debe tener entre 3 y 100 caracteres",
    "email": "Debe proporcionar una dirección de correo electrónico válida",
    "rol": "El rol es obligatorio"
  }
}
```

### Crear usuario válido

```bash
curl -i -X POST http://localhost:8080/api/usuarios \
     -H "Content-Type: application/json" \
     -d '{"nombre": "Ana Perez", "email": "ana@correo.com", "rol": "ROLE_USER"}'
```

Esperado: `201 Created`.

### DELETE con USER → 403; con ADMIN → 204

```bash
curl -i -X DELETE http://localhost:8080/api/usuarios/1 \
     -H "Authorization: Bearer TOKEN_ROLE_USER"
```

```bash
curl -i -X DELETE http://localhost:8080/api/usuarios/1 \
     -H "Authorization: Bearer TOKEN_ROLE_ADMIN"
```

## 13.3 Terminal — pedidos (con JWT)

Crear pedido para el usuario `1`:

```bash
curl -i -X POST http://localhost:8080/api/pedidos/usuario/1 \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer PEGA_TOKEN" \
     -d '{"descripcion": "Teclado mecanico", "total": 250000}'
```

Filtrar:

```bash
curl -i "http://localhost:8080/api/pedidos/filtrar?minimo=1000" \
     -H "Authorization: Bearer PEGA_TOKEN"
```

## 13.4 Postman — colección mínima

1. Crea un **Environment** (ej. `local-spring`) y selecciónalo arriba a la derecha.
2. Variable: `baseUrl` = `http://localhost:8080`.
3. Variable vacía: `jwt_token`.

### Petición 1 — Login

- Método: `POST`
- URL: `{{baseUrl}}/api/auth/login`
- Body → raw → JSON:

```json
{ "username": "admin", "password": "admin123" }
```

- Pestaña **Tests** / **Post-response Script**:

```javascript
pm.environment.set("jwt_token", pm.response.json().token);
```

Send. En el environment debe aparecer el token.

Duplica la petición para USER (`user` / `user123`) si quieres alternar roles (sobrescribe `jwt_token`).

### Petición 2 — Listar usuarios

- `GET` `{{baseUrl}}/api/usuarios`
- Authorization → Type **Bearer Token** → Token: `{{jwt_token}}`

Admin: 200. User: 403.

### Petición 3 — Crear usuario (validación)

- `POST` `{{baseUrl}}/api/usuarios`
- Authorization: No Auth (ruta pública)
- Body JSON inválido primero (`nombre: "A"`), luego válido.

### Petición 4 — Pedidos

- `GET` `{{baseUrl}}/api/pedidos` con Bearer `{{jwt_token}}`
- `POST` `{{baseUrl}}/api/pedidos/usuario/1` con el mismo Bearer y body JSON de pedido.

### Petición 5 — DELETE

- `DELETE` `{{baseUrl}}/api/usuarios/1` con Bearer. Cambia login USER vs ADMIN y observa 403 vs 204.

Así no se copia el token a mano: cada login actualiza `jwt_token`.

## 13.5 Consola del navegador (opcional)

Abre `http://localhost:8080`, pulsa `F12` → Console.

```javascript
localStorage.setItem('token', 'PEGA_TOKEN_REAL');
const token = localStorage.getItem('token');
const payload = JSON.parse(atob(token.split('.')[1]));
console.log(payload);

fetch('/api/usuarios', {
  headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
}).then(r => console.log(r.status));
```

**Application → Local Storage:** borrar el token simula logout en cliente (stateless: el servidor no invalida nada).

---

# 14. Herramientas visuales y analogía didáctica

## jwt.io

1. Pega el token de login en [jwt.io](https://jwt.io/).
2. Header (algoritmo), payload (`sub`, `rol`, `iat`, `exp`), signature.
3. Cambia una letra del payload: **Invalid Signature**.

## CyberChef

Decodifica el segmento del payload (Base64Url: `-` / `_`, sin `=`).

## Analogía del pasaporte

- Sesión tradicional: el guardia llama a recepción en cada puerta.
- JWT: pasaporte (payload = datos, `exp` = vencimiento, firma = holograma). El guardia no llama al emisor si el sello es íntegro → **stateless**.

## Reto express (10 minutos)

1. Login y copiar token.
2. Pegarlo en jwt.io.
3. ¿Cuándo expira (`exp` Unix → fecha)? ¿Qué ocurre si cambias la última letra y lo mandas por `curl`?

---

# 15. Errores comunes

| Error | Causa | Qué hacer |
|-------|--------|-----------|
| `Connection refused` / `localhost:5432` | PostgreSQL apagado o puerto ocupado | `docker compose up -d` **o** servicio local; no ambos en 5432 |
| `password authentication failed` | `.env` distinto de `application.properties` | Unificar usuario/clave/BD |
| `401` en `/api/auth/login` | Body distinto de `admin`/`admin123` o `user`/`user123` | Revisar JSON |
| `403` en rutas protegidas | Falta Bearer, token de USER en ruta ADMIN, o token vencido (1 h) | Reloguear con el rol correcto |
| `ClassNotFoundException` / `Jwts` | Falta JJWT o Maven no recargó | `mvnw clean install` |
| Tokens viejos fallan al reiniciar | `Keys.secretKeyFor` genera clave nueva | Esperado en esta versión de clase |
| `./mvnw: Permission denied` | Sin `+x` | `chmod +x mvnw` |
| `mvnw.cmd` no se reconoce | No estás en la raíz | `cd` hasta el `pom.xml` |
| PowerShell `curl` raro | Alias a `Invoke-WebRequest` | `curl.exe` o CMD |
| CORS / `Failed to fetch` | Origen distinto a `localhost:8080` | Abrir esa URL antes del `fetch` |
| `sed` duplica `</dependencies>` | El replace ya se ejecutó | Editar `pom.xml` a mano |
| Validación no dispara | Falta starter-validation o falta `@Valid` | Sección 12 |

---

# 16. Resumen del curso

Temario cubierto:

- Arquitectura en capas (entity, repository, service, controller, DTO)
- Spring Data JPA + Hibernate + PostgreSQL
- Docker Compose y PostgreSQL local
- Spring Security stateless
- JWT (JJWT 0.11.5): firma, claims, filtro Bearer
- Roles (`ROLE_USER` / `ROLE_ADMIN`) en token y en `authorizeHttpRequests`
- Jakarta Validation + `@RestControllerAdvice`

Evoluciones posteriores (fuera de esta guía, no repetidas arriba): `PasswordEncoder` + usuarios en BD, clave JWT fija en properties, refresh tokens, migraciones.

```text
Cliente HTTP
    → JWT Filter + SecurityFilterChain
    → @RestController (+ @Valid)
    → @RestControllerAdvice (si hay error)
    → @Service
    → JpaRepository
    → Hibernate
    → PostgreSQL
```
