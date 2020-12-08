// run manually with -x compileKotlin when you need to regenerate
tasks.register<JavaExec>("generateAntlrSources") {
    main = "org.antlr.v4.Tool"

    args = listOf(
            "-o", "src/main/java/org/openrewrite/xml/internal/grammar",
            "-package", "org.openrewrite.xml.internal.grammar",
            "-visitor"
    ) + fileTree("src/main/antlr").matching { include("**/*.g4") }.map { it.path }

    classpath = sourceSets["main"].runtimeClasspath
}

dependencies {
    api(project(":rewrite-core"))

    implementation("org.antlr:antlr4:4.8-1")

    implementation(enforcedPlatform("com.fasterxml.jackson:jackson-bom:latest.release"))
    api("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    testImplementation(project(":rewrite-test"))
}
