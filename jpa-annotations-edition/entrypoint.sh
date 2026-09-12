#!/bin/bash
set -e

HOST="${DB_HOST:-db}"
PORT="${DB_PORT:-5432}"
MAIN_CLASS="${MAIN_CLASS:-com.corp.learning.hibernate.demo.RunAllDemos}"

echo "Waiting for database at ${HOST}:${PORT} ..."
tries=0
until bash -c "echo > /dev/tcp/${HOST}/${PORT}" 2>/dev/null; do
  tries=$((tries + 1))
  if [ "$tries" -gt 60 ]; then
    echo "Database never became reachable, giving up."
    exit 1
  fi
  sleep 1
done
echo "Database is reachable. Running ${MAIN_CLASS} ..."

# Plain `java` process, isolated from Maven's own JVM/classloader on purpose -
# see the maven-shade-plugin comment in pom.xml for why `mvn exec:java` was
# dropped in favor of this.
exec java -cp /app/target/jpa-annotations-edition.jar "${MAIN_CLASS}"
