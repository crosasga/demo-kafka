# **Kafka Spring Boot Project README**

## **Descripción del Proyecto**
Este proyecto es una aplicación Spring Boot que implementa productores y consumidores de mensajes Kafka, con integración a PostgreSQL para almacenar mensajes procesados y utilizando Swagger para documentar la API. Además, se gestionan configuraciones mediante archivos `application.yml` y anotaciones de propiedades.

---

## **Requisitos Previos**

- **Java** 21 o superior
- **Maven**
- **PostgreSQL**
- **Apache Kafka**
- Docker (opcional para correr Kafka y PostgreSQL)

---

## **Instalación**

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/tu-repo/demo-kafka.git
   cd demo-kafka
   ```

2. **Construir el proyecto:**
   ```bash
   mvn  clean package -Dmaven.test.skip=true
   docker-compose down -v
   docker builder prune --force
   docker-compose up --build
   ```

3. **Configurar `application.yml` con los parámetros de la base de datos y el servidor Kafka.**

---

## **Estructura del Proyecto**
```
com.banco.central.demo
├── controller      // Controladores de la API REST
├── config          // Configuración de Kafka y propiedades
├── dto             // Data Transfer Objects
├── entity          // Entidades JPA para PostgreSQL
├── repository      // Repositorios JPA
├── service         // Servicios de lógica de negocio
└── DemoApplication // Clase principal para ejecutar la aplicación
```
---

## **Servicios de Kafka**

### **Productor Kafka (`KafkaProducerServiceImpl`)**
```java
@Service
@RequiredArgsConstructor
public class KafkaProducerServiceImpl {

    private final KafkaTemplate<String, MessageRequestDTO> kafkaTemplate;

    public String sendMessage(MessageRequestDTO messageRequestDTO) {
        String uniqueId = UUID.randomUUID().toString();
        kafkaTemplate.send("banco-topic", uniqueId, messageRequestDTO);
        return uniqueId;
    }
}
```

### **Consumidor Kafka (`KafkaConsumerServiceImpl`)**
```java
@Service
@RequiredArgsConstructor
public class KafkaConsumerServiceImpl {

    private final KafkaProperties kafkaProperties;

    @KafkaListener(topics = "#{'${spring.kafka.consumer.topic}'}", groupId = "#{'${spring.kafka.consumer.group-id}'}")
    public void listen(MessageRequestDTO message) {
        System.out.println("Mensaje recibido: " + message);
    }
}
```
---
## **Swagger UI**
- **URL de acceso a la documentación:** `http://localhost:8080/swagger-ui.html`

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

---
# Endpoints de la API de Kafka

Esta API permite interactuar con mensajes en Kafka mediante operaciones de envío, obtención, eliminación y verificación de estado.

## Tabla de Endpoints

| Método | Endpoint             | Descripción                                | Parámetros                                   | Ejemplo de Request Body | Respuesta Exitosa               |
|--------|----------------------|--------------------------------------------|----------------------------------------------|-------------------------|----------------------------------|
| `POST` | `/api/v1/queues`      | Enviar mensaje a Kafka con un ID único     | **Body:** `MessageRequestDTO` (requerido)    | `{ "message": "Hola" }`  | `200 OK` con `id`, `message`, `status` |
| `GET`  | `/api/v1/queues/{id}` | Obtener mensaje por ID                     | **PathVariable:** `id` (String, requerido)   | -                       | `200 OK` con mensaje correspondiente |
| `GET`  | `/api/v1/queues/all`  | Obtener todos los mensajes de la cola      | Ninguno                                      | -                       | `200 OK` con lista de mensajes |
| `DELETE` | `/api/v1/queues/{id}` | Eliminar mensaje por ID                   | **PathVariable:** `id` (String, requerido)   | -                       | `200 OK` si se eliminó con éxito, `204 No Content` si no existe |
| `GET`  | `/api/v1/queues/health` | Verificar estado del API                  | Ninguno                                      | -                       | `200 OK` con mensaje `"Kafka API está funcionando"` |

## **Ejemplo de `Mensaje de respuesta en creacion`**

```json
{
   "status": "success",
   "message": "Mensaje enviado con ID",
   "id": "cbb69f7f-2cd0-422c-bf39-36919393c5de"
}

```

---
## **Prueba con Postman**
### **Enviar mensaje a Kafka**
- **Método:** `POST`
- **URL:** `http://localhost:8080/api/v1/queues`
- **Body (JSON):**
```json
{
  "id": "12345",
  "message": "Mensaje de prueba",
  "timestamp": "2024-12-23T11:19:32Z",
  "metadata": {
    "source": "app1",
    "type": "notification"
  }
}
```

---

## **Comandos Útiles para Kafka**
### **Reiniciar offsets:**
```bash
kafka-consumer-groups --bootstrap-server localhost:9092 --group banco-busqueda --reset-offsets --to-earliest --execute --topic banco-topic
```
### **Ver mensajes:**
```bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic banco-topic --from-beginning
```

---

## **Consideraciones Finales**
- El campo `metadata` debe ser siempre un JSON válido.
- Verifica que el topic esté correctamente creado antes de enviar mensajes.
- La configuración de `auto-offset-reset` debe ser `earliest`, `latest` o `none`.

---

## **Mejoras Futuras**
- Implementar soporte para múltiples tópicos dinámicos.
- Agregar autenticación y manejo de roles.
- Optimizar el manejo de errores y reintentos con `Resilience4j`.

---

## **Autores**
- **Nombre:** Cristian Rosas
- **Proyecto:** Kafka Spring Boot + PostgreSQL.

