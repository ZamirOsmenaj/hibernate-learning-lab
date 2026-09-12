# Using the "eclipse-temurin-8" flavor, not "openjdk-8": Docker Hub's official
# maven image retired every plain openjdk-* tag. eclipse-temurin-8 is the
# current, maintained way to get Maven + JDK 8, matching this project's
# maven.compiler.target=1.8.
FROM maven:3.9.16-eclipse-temurin-8

WORKDIR /app

# Cache dependency resolution in its own layer so code edits don't re-download the world
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src

# Build the runnable fat jar (target/jpa-annotations-edition.jar). We run THIS
# with a plain `java` process at container start (see entrypoint.sh), not
# `mvn exec:java` - see the comment on the maven-shade-plugin in pom.xml for why.
RUN mvn -q -B package -DskipTests

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
