# Guía de Proyecto: API REST con Spring Boot, Spring Data JPA y PostgreSQL

> **Curso:** Java Backend con Spring Boot  
> **Objetivo:** Construir una API REST utilizando Spring Boot, Spring Data JPA y PostgreSQL siguiendo una arquitectura en capas.

---

# Tabla de Contenido

1. Arquitectura del Proyecto
2. Flujo Interno de la Aplicación
3. Explicación de Clases y Anotaciones
4. Entidades (Entity)
5. Repositorios (Repository)
6. Servicios (Service)
7. Controladores (Controller)
8. DTOs (Data Transfer Objects)
9. Consultas Personalizadas (@Query)
10. Flujo Completo de una Petición
11. Clases Nativas de Java Utilizadas
12. Configuración de PostgreSQL
13. application.properties
14. Resumen General

---

# 1. Arquitectura del Proyecto

El proyecto sigue una arquitectura en capas, separando responsabilidades para facilitar el mantenimiento, reutilización y escalabilidad del código.

```text
project/
│
├── src/
│   └── main/
│       ├── java/
│       │
│       │   └── com/example/project/
│       │
│       │       ├── ProjectApplication.java
│       │
│       │       ├── entity/
│       │       │     ├── Usuario.java
│       │       │     └── Pedido.java
│       │       │
│       │       ├── repository/
│       │       │     ├── UsuarioRepository.java
│       │       │     └── PedidoRepository.java
│       │       │
│       │       ├── service/
│       │       │     ├── UsuarioService.java
│       │       │     └── PedidoService.java
│       │       │
│       │       ├── controller/
│       │       │     ├── UsuarioController.java
│       │       │     └── PedidoController.java
│       │       │
│       │       └── dto/
│       │             ├── UsuarioDTO.java
│       │             └── PedidoDTO.java
│       │
│       └── resources/
│             └── application.properties
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── docker-compose.yml
```

---

# 2. Flujo Interno de la Aplicación

Cuando un cliente realiza una petición HTTP, esta recorre diferentes capas del sistema.

```text
Cliente
(Postman / Navegador / Frontend)
            │
            ▼
 Apache Tomcat (Servidor Web)
            │
            ▼
Controller
(@RestController)
            │
            ▼
Service
(@Service)
            │
            ▼
Repository
(JpaRepository)
            │
            ▼
Hibernate (ORM)
            │
            ▼
PostgreSQL
```

Cada capa tiene una responsabilidad específica.

| Capa | Responsabilidad |
|-------|-----------------|
| Controller | Recibe peticiones HTTP |
| Service | Contiene la lógica del negocio |
| Repository | Accede a la base de datos |
| Hibernate | Convierte objetos Java en SQL |
| PostgreSQL | Almacena la información |

---

# 3. Explicación General del Proyecto

Durante el desarrollo construiremos una API REST donde:

- Un **Usuario** puede tener muchos **Pedidos**.
- Cada **Pedido** pertenece a un único **Usuario**.

Modelo:

```text
Usuario
   │
   │ 1
   │
   ├───────────────┐
   ▼               ▼
Pedido          Pedido
```

---

# 4. Entidades (Entity)

Las entidades representan las tablas de la base de datos.

## Usuario.java

Representa la tabla:

```text
usuario
```

Anotaciones utilizadas:

### @Entity

Convierte una clase Java en una tabla de la base de datos.

```java
@Entity
```

---

### @Table

Permite definir el nombre de la tabla.

```java
@Table(name = "usuarios")
```

---

### @Id

Define la clave primaria.

```java
@Id
private Long id;
```

---

### @GeneratedValue

Genera automáticamente el identificador.

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

Hibernate utilizará:

```sql
BIGSERIAL
```

o

```sql
IDENTITY
```

dependiendo del motor de base de datos.

---

### @Column

Permite personalizar una columna.

Ejemplos:

```java
@Column(nullable = false)
```

```java
@Column(unique = true)
```

```java
@Column(length = 100)
```

---

## Relaciones

### @OneToMany

Representa la relación Uno a Muchos.

```java
@OneToMany(mappedBy="usuario")
private List<Pedido> pedidos;
```

Significa:

Un usuario posee muchos pedidos.

---

### mappedBy

Indica cuál atributo controla la relación.

```java
mappedBy="usuario"
```

No crea una tabla intermedia.

---

### cascade

Permite propagar operaciones.

```java
cascade = CascadeType.ALL
```

Si guardamos un Usuario también guarda automáticamente los Pedidos.

---

### orphanRemoval

```java
orphanRemoval = true
```

Si eliminamos un Pedido de la lista, Hibernate también lo elimina de PostgreSQL.

---

## Pedido.java

Representa la tabla:

```text
pedido
```

---

### @ManyToOne

Muchos pedidos pertenecen a un usuario.

```java
@ManyToOne
private Usuario usuario;
```

---

### FetchType.LAZY

```java
@ManyToOne(fetch = FetchType.LAZY)
```

Carga el usuario únicamente cuando sea necesario.

Optimiza el rendimiento.

---

### @JoinColumn

Define la clave foránea.

```java
@JoinColumn(name="usuario_id")
```

Hibernate crea automáticamente:

```text
usuario_id
```

como Foreign Key.

---

# 5. Repository

Los Repository permiten acceder a PostgreSQL sin escribir SQL básico.

Ejemplo:

```java
public interface UsuarioRepository
extends JpaRepository<Usuario, Long>{

}
```

---

## JpaRepository

Al extender JpaRepository obtenemos automáticamente métodos como:

```java
save()

findAll()

findById()

delete()

deleteById()

existsById()

count()
```

Sin escribir código adicional.

---

## Consultas Derivadas

Spring interpreta el nombre del método.

Ejemplo:

```java
findByCorreo(String correo)
```

Spring genera automáticamente el SQL.

Otro ejemplo:

```java
findByNombre(String nombre)
```

No escribimos SQL.

---

# 6. Service

Los Services contienen la lógica del negocio.

```java
@Service
public class UsuarioService{
}
```

---

## @Service

Marca la clase como servicio.

Spring la administra automáticamente.

---

## Inyección de Dependencias

Se realiza mediante constructor.

```java
private final UsuarioRepository repository;
```

Esto desacopla las clases.

---

## @Transactional

```java
@Transactional
```

Si ocurre un error:

Spring realiza:

```text
ROLLBACK
```

Ningún dato queda parcialmente guardado.

---

## @Transactional(readOnly=true)

Optimiza consultas de lectura.

```java
@Transactional(readOnly=true)
```

---

# 7. Controller

Los Controllers exponen la API REST.

```java
@RestController
@RequestMapping("/api/usuarios")
```

---

## @RestController

Recibe peticiones HTTP.

Responde JSON automáticamente.

---

## @RequestMapping

Define la ruta principal.

```java
/api/usuarios
```

---

## @GetMapping

Obtiene información.

```java
@GetMapping
```

---

## @PostMapping

Guarda información.

```java
@PostMapping
```

---

## @DeleteMapping

Elimina registros.

```java
@DeleteMapping("/{id}")
```

---

## @RequestBody

Convierte automáticamente el JSON recibido a un objeto Java.

```java
@RequestBody Usuario usuario
```

---

## @PathVariable

Obtiene variables desde la URL.

Ejemplo:

```text
/api/usuarios/5
```

```java
@PathVariable Long id
```

---

## @RequestParam

Obtiene parámetros.

Ejemplo:

```text
/api/pedidos?minimo=500
```

---

## ResponseEntity

Permite devolver:

- Datos
- Código HTTP

Ejemplo:

```java
ResponseEntity.ok(...)
```

---

# 8. DTO (Data Transfer Object)

Los DTO permiten enviar únicamente la información necesaria al cliente.

Evitan:

- Relaciones infinitas
- Información innecesaria
- Problemas de serialización

Ejemplo:

```text
Pedido

↓

PedidoDTO
```

---

# 9. Consultas Personalizadas

Además de las consultas automáticas podemos crear consultas propias.

## JPQL

Trabaja con objetos Java.

```java
@Query("""
SELECT p
FROM Pedido p
WHERE p.total > :minimo
""")
```

JPQL consulta entidades.

No consulta tablas.

---

## SQL Nativo

También podemos utilizar SQL tradicional.

```java
@Query(
value="SELECT * FROM pedidos",
nativeQuery=true)
```

Aquí sí trabajamos directamente con PostgreSQL.

---

# 10. Flujo Completo de una Petición

Cuando un cliente envía un POST para guardar un pedido:

```text
Cliente

↓

POST /api/pedidos

↓

Controller

↓

Service

↓

Repository

↓

Hibernate

↓

SQL

↓

PostgreSQL

↓

Registro Guardado
```

Posteriormente:

```text
PostgreSQL

↓

Hibernate

↓

Objeto Java

↓

JSON

↓

Cliente
```

---

# 11. Clases Nativas de Java Utilizadas

## BigDecimal

Ideal para dinero.

Evita errores de precisión.

```java
BigDecimal total;
```

---

## LocalDateTime

Representa fecha y hora.

```java
LocalDateTime.now();
```

---

## Optional

Evita NullPointerException.

```java
Optional<Usuario>
```

---

## List

Representa colecciones.

```java
List<Pedido>
```

---

## ArrayList

Implementación de List.

```java
new ArrayList<>();
```

---

## Stream

Transforma colecciones.

```java
.stream()
.map(...)
.collect(...)
```

---

# 12. Configuración de PostgreSQL

## Opción A - Docker

```properties
spring.application.name=project

spring.datasource.url=jdbc:postgresql://localhost:5432/projectdb
spring.datasource.username=postgres
spring.datasource.password=123456

spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.hibernate.ddl-auto=update
```

---

## Opción B - PostgreSQL Instalado Localmente

Crear primero la base de datos.

```sql
CREATE DATABASE projectdb;
```

Configurar:

```properties
spring.application.name=project

spring.datasource.url=jdbc:postgresql://localhost:5432/projectdb

spring.datasource.username=postgres

spring.datasource.password=SU_PASSWORD

spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.jpa.hibernate.ddl-auto=update
```

---

# 13. Explicación de ddl-auto=update

```properties
spring.jpa.hibernate.ddl-auto=update
```

Esta propiedad indica a Hibernate:

- Leer las entidades Java.
- Compararlas con PostgreSQL.
- Crear tablas si no existen.
- Agregar columnas nuevas.
- Crear claves foráneas.
- Actualizar el esquema automáticamente.

Gracias a esta opción no necesitamos escribir manualmente:

```sql
CREATE TABLE
```

ni

```sql
ALTER TABLE
```

---

# 14. Resumen General

El flujo completo del proyecto puede resumirse de la siguiente manera:

```text
          Cliente HTTP
                │
                ▼
      @RestController
                │
                ▼
          @Service
                │
                ▼
     JpaRepository
                │
                ▼
     Hibernate / JPA
                │
                ▼
         PostgreSQL
                │
                ▼
     Datos Persistidos
```

## Tecnologías Utilizadas

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate ORM
- PostgreSQL
- Maven
- Docker
- VS Code

---

## Resultado Final

Durante el curso se desarrolló una **API REST profesional** basada en una **arquitectura en capas**, aplicando buenas prácticas de desarrollo backend con **Spring Boot**, **Spring Data JPA**, **Hibernate** y **PostgreSQL**, utilizando relaciones entre entidades, repositorios, servicios, controladores, DTOs y consultas personalizadas para construir un sistema mantenible, escalable y preparado para proyectos reales.


# Tarea implementa el JWT en el repo

Paso 1 implementar el jwt en el archivo pom.xml

Paso 2 Crear el jwt.Util.java
```bash
src/main/java/com/example/project/config/JwtUtils.java

```

Paso 3 JwtAuthenticationFilter.java

```bash
src/main/java/com/example/project/config/JwtAuthenticationFilter.java

```

Paso 4 AuthDTO.java y AuthController.java
1 DTO de autenticacion (AuthDTO.java)
```bash
src/main/java/com/example/project/dto/AuthDTO.java

```

4.1 Controlador de Autenticación (AuthController.java)
```bash
src/main/java/com/example/project/controller/AuthController.java
```
Paso 5 
```bash
rc/main/java/com/example/project/config/SecurityConfig.java
```
