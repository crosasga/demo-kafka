# Usar OpenJDK 21
FROM eclipse-temurin:21-jdk-alpine

# Crear directorio para la aplicación
WORKDIR /app


# Copiar el JAR generado por Maven
#COPY ./target/demo-0.0.1-SNAPSHOT.jar app.jar

# Dar permisos de ejecución al archivo JAR dentro del contenedor
#RUN chmod +x app.jar

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]

# Copiar el script entrypoint.sh
COPY ./entrypoint.sh /entrypoint.sh


# Dar permisos de ejecución al script
RUN chmod +x /entrypoint.sh

# Ejecutar el script de entrada
ENTRYPOINT ["/entrypoint.sh"]

# Establecer zona horaria
ENV TZ America/Santiago

# Puerto de escucha
EXPOSE 8080

