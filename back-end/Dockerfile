# ---------- Etapa 1: build ----------
# Compilamos acá con Maven + JDK. Esta imagen NO es la que se ejecuta en producción,
# solo se usa para generar el .jar.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos primero solo el pom.xml y descargamos las dependencias:
# así, mientras no cambies el pom.xml, Docker reutiliza esta capa cacheada
# y no vuelve a descargar todo internet cada vez que cambiás una clase Java.
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Ahora sí copiamos el código fuente y compilamos el jar final.
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Etapa 2: runtime ----------
# Imagen final, liviana: solo el JRE (no el JDK completo) + el jar ya compilado.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/ReporteExcelLicitaciones-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
