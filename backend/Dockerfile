# Imagem da API REST para o Render (Lab04S02 - docs/sprint-05/diagrama-implantacao.puml).
# O Render não tem ambiente Java próprio, por isso a API é publicada como contêiner Docker.

# --- Etapa 1: compila o JAR com o Maven Wrapper e o JDK 21 ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
# Os testes rodam antes do push (mvnw test); aqui só se gera o pacote.
RUN chmod +x mvnw && ./mvnw -B -q package -DskipTests

# --- Etapa 2: executa só com o JRE, em uma imagem menor e sem as ferramentas de compilação ---
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /build/target/*.jar app.jar
USER app

# Horário de Brasília: data das transações no extrato, semestre do crédito (RN04) e horário do agendamento.
ENV TZ=America/Sao_Paulo
# O Render informa a porta na variável PORT, lida por server.port em application.properties.
EXPOSE 8080

# A instância gratuita tem 512 MB e 0,1 CPU: heap limitada a 60% da memória, compilação JIT mais leve
# (inicia mais rápido) e, se a memória acabar, o processo termina para o Render reiniciá-lo.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=60", "-XX:TieredStopAtLevel=1", "-XX:+ExitOnOutOfMemoryError", \
            "-Duser.timezone=America/Sao_Paulo", "-jar", "app.jar"]
