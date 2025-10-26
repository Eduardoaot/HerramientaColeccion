# Usar una imagen base de Java 21
FROM eclipse-temurin:21.0.3_9-jdk

# Informar el puerto donde se ejecuta el contenedor (informativo)
EXPOSE 8080

# Definir el directorio raíz del contenedor
WORKDIR /app

# Copiar los archivos necesarios para construir la aplicación
COPY ./pom.xml /app
COPY ./.mvn /app/.mvn
COPY ./mvnw /app

RUN chmod +x ./mvnw

# Descargar las dependencias de Maven
RUN ./mvnw dependency:go-offline

# Copiar el código fuente de la aplicación
COPY ./src /app/src

# Construir la aplicación (sin ejecutar pruebas)
RUN ./mvnw clean install -DskipTests

# Levantar la aplicación cuando el contenedor inicie
ENTRYPOINT ["java", "-jar", "/app/target/coleccion-0.0.1.jar"]