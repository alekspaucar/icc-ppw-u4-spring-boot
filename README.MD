# Práctica 1 - Spring Boot

## 1. Verificación de Java

![Java Version](assets/java-version.png)

Se verificó la una versión compatible de Java mediante el comando `java -version`.

---

## 2. Ejecución del servidor Spring Boot

![Servidor](assets/servidor.png)

La aplicación inició correctamente utilizando `./gradlew bootRun` y Tomcat quedó escuchando en el puerto 8080.

---

## 3. Endpoint /api/status

![Endpoint](assets/status-endpoint.png)

El endpoint responde correctamente en formato JSON mostrando el estado del servicio.

---

## 4. Verificación del controlador

![Controlador](assets/status-controller.png)

Se verificó la existencia del archivo `StatusController.java`.

---

## 5. Explicación

### Funcionamiento del endpoint

El endpoint `/api/status` permite comprobar que la API se encuentra en ejecución. Cuando recibe una petición GET devuelve un objeto JSON con información del servicio, estado y fecha actual.

### Función de Spring Boot

Spring Boot simplifica el desarrollo de aplicaciones backend mediante configuración automática, dependencias predefinidas y un servidor embebido Tomcat, permitiendo crear APIs REST de manera rápida y organizada para una mejor funcionalidad.