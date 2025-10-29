# Release process

1. Update the `maven-artifactVersion` in `gradle/libs.versions.toml`.
2. Update the `ACTITO_VERSION` in `actito/src/main/java/com/actito/internal/Version.kt`.
3. Update the `CHANGELOG.md`.
4. Push the changes to the repo.
5. Run `./gradlew clean`.
6. Run `./gradlew publishAndReleaseToMavenCentral`.
7. Create a GitHub release with the contents of the `CHANGELOG.md`.
