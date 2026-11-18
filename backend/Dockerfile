# syntax=docker/dockerfile:1

# Build: compila o jar dentro da imagem (os testes e as análises rodam no CI)
FROM eclipse-temurin:25-jdk AS build
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && ./mvnw -B -q package -DskipTests -Dcheckstyle.skip -Dspotbugs.skip

# Runtime: apenas o JRE e o jar, rodando com usuário sem privilégios
FROM eclipse-temurin:25-jre
RUN groupadd --system --gid 10001 musicando \
    && useradd --system --uid 10001 --gid musicando --home-dir /app musicando \
    && mkdir -p /musicando-backend/database \
    && chown musicando:musicando /musicando-backend/database
WORKDIR /app
COPY --from=build /build/target/musicando-*.jar app.jar
USER 10001
ENV SPRING_PROFILES_ACTIVE=prd \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
VOLUME /musicando-backend/database
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
