plugins {
    id("java")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("commons-httpclient:commons-httpclient:3.1")
    implementation("com.google.code.gson:gson:2.10")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("org.json:json:20220924")

    // https://mvnrepository.com/artifact/com.github.rockswang/java-curl
    implementation("com.github.rockswang:java-curl:1.2.2.2")

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}

group = "io.github.pedroagrs"
version = "1.0-SNAPSHOT"
