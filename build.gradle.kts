import io.flamingock.build.VersionManager
import io.flamingock.build.PrintVersionTask

plugins {
    `java-library`
    `maven-publish`
    id("com.diffplug.spotless") version "6.25.0"
    id("org.jreleaser") version "1.15.0"
}


group = "io.flamingock"
val declaredVersion = "1.3.2-SNAPSHOT"
version = VersionManager.resolveVersion(declaredVersion, project.hasProperty("release"))

repositories {
    mavenLocal()
    mavenCentral()
}

val generalUtilVersion = "1.5.1"
dependencies {
    implementation("io.flamingock:flamingock-general-util:$generalUtilVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito:mockito-inline:4.11.0")
}

description = "Public API for creating Flamingock change templates"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    withSourcesJar()
    withJavadocJar()
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "flamingock-template-api"

            pom {
                name.set("flamingock-template-api")
                description.set("Public API for creating Flamingock change templates")
                url.set("https://flamingock.io")

                organization {
                    name.set("Flamingock")
                    url.set("https://flamingock.io")
                }

                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/flamingock/flamingock-java-template-api/issues")
                }

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("dieppa")
                        name.set("Antonio Perez Dieppa")
                        email.set("dieppa@flamingock.io")
                    }
                    developer {
                        id.set("osantana")
                        name.set("Oscar Santana")
                        email.set("osantana@flamingock.io")
                    }
                    developer {
                        id.set("bercianor")
                        name.set("Berciano Ramiro")
                        email.set("bercianor@flamingock.io")
                    }
                    developer {
                        id.set("dfrigolet")
                        name.set("Daniel Frigolet")
                        email.set("dfrigolet@flamingock.io")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/flamingock/flamingock-java-template-api.git")
                    developerConnection.set("scm:git:ssh://github.com/flamingock/flamingock-java-template-api.git")
                    url.set("https://github.com/flamingock/flamingock-java-template-api")
                }
            }
        }
    }
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

val licenseHeaderText = """/*
 * Copyright ${'$'}YEAR Flamingock (https://www.flamingock.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */"""

spotless {
    java {
        target("src/**/*.java")
        licenseHeader(licenseHeaderText)
    }

    kotlin {
        target("src/**/*.kt")
        licenseHeader(licenseHeaderText)
    }
}

afterEvaluate {
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn.removeIf { it.toString().contains("spotless") }
    }
    tasks.matching { it.name.startsWith("spotless") && it.name.contains("Check") }.configureEach {
        group = "verification"
        description = "Check license headers (manual task - not part of build)"
    }
}

// Part 1: Release management config — always present (used by jreleaserRelease)
jreleaser {
    project {
        inceptionYear.set("2024")
        authors.set(setOf("dieppa", "osantana", "bercianor", "dfrigolet"))
        description.set("Public API for creating Flamingock change templates")
    }
    gitRootSearch.set(true)
    release {
        github {
            update {
                enabled.set(true)
                sections.set(setOf(org.jreleaser.model.UpdateSection.TITLE, org.jreleaser.model.UpdateSection.BODY, org.jreleaser.model.UpdateSection.ASSETS))
            }
            prerelease {
                pattern.set("^(0\\..*|.*-(beta\\.?\\d*|snapshot\\.?\\d*|alpha\\.?\\d*|rc\\.?\\d*|RC\\.?\\d*)\$)")
            }
            changelog {
                enabled.set(true)
                formatted.set(org.jreleaser.model.Active.ALWAYS)
                sort.set(org.jreleaser.model.Changelog.Sort.DESC)
                links.set(true)
                preset.set("conventional-commits")
                releaseName.set("Release {{tagName}}")
                content.set("""
                    ## Changelog
                    {{changelogChanges}}
                    {{changelogContributors}}
                """.trimIndent())
                categoryTitleFormat.set("### {{categoryTitle}}")
                format.set(
                    """|- {{commitShortHash}}
                       | {{#commitIsConventional}}
                       |{{#conventionalCommitIsBreakingChange}}:rotating_light: {{/conventionalCommitIsBreakingChange}}
                       |{{#conventionalCommitScope}}**{{conventionalCommitScope}}**: {{/conventionalCommitScope}}
                       |{{conventionalCommitDescription}}
                       |{{#conventionalCommitBreakingChangeContent}} - *{{conventionalCommitBreakingChangeContent}}*{{/conventionalCommitBreakingChangeContent}}
                       |{{/commitIsConventional}}
                       |{{^commitIsConventional}}{{commitTitle}}{{/commitIsConventional}}
                       |{{#commitHasIssues}}, closes{{#commitIssues}} {{issue}}{{/commitIssues}}{{/commitHasIssues}}
                       |{{#contributorName}} ({{contributorName}}){{/contributorName}}
                    |""".trimMargin().replace("\n", "").replace("\r", "")
                )
                contributors {
                    enabled.set(true)
                    format.set("- {{contributorName}} ({{contributorUsernameAsLink}})")
                }
            }
        }
    }
}

// Part 2: Deploy config — only when deploying to Maven Central (not needed for jreleaserRelease)
val isReleasing = gradle.startParameter.taskNames.any {
    it in listOf("jreleaserFullRelease", "jreleaserDeploy", "publish")
}

if (isReleasing) {
    jreleaser {
        signing {
            active.set(org.jreleaser.model.Active.ALWAYS)
            armored = true
            enabled = true
        }
        release {
            github {
                skipRelease.set(true)
                skipTag.set(true)
            }
        }
        deploy {
            maven {
                mavenCentral {
                    create("sonatype") {
                        active.set(org.jreleaser.model.Active.ALWAYS)
                        applyMavenCentralRules.set(true)
                        url.set("https://central.sonatype.com/api/v1/publisher")
                        stagingRepository("build/staging-deploy")
                        maxRetries.set(90)
                        retryDelay.set(20)
                    }
                }
            }
        }
    }
}

tasks.register("createStagingDeployFolder") {
    doLast {
        mkdir(layout.buildDirectory.dir("staging-deploy"))
    }
}

tasks.matching { it.name == "publish" }.configureEach {
    finalizedBy("createStagingDeployFolder")
}

tasks.register<PrintVersionTask>("printVersion")
