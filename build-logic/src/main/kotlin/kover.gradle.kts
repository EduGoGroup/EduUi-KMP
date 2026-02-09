/**
 * Convention plugin for Kover code coverage configuration.
 *
 * This plugin configures Kover (Kotlin Code Coverage) for multiplatform projects
 * with sensible defaults and consistent reporting across all modules.
 *
 * Features:
 * - HTML and XML report generation
 * - Coverage verification rules with minimum thresholds
 * - Exclusion of test and generated code
 * - Support for all Kotlin Multiplatform targets
 *
 * Usage:
 * ```kotlin
 * plugins {
 *     id("kover")
 * }
 * ```
 *
 * Reports are generated at:
 * - HTML: build/reports/kover/html/index.html
 * - XML: build/reports/kover/report.xml
 *
 * Run coverage:
 * ```bash
 * ./gradlew koverHtmlReport
 * ./gradlew koverXmlReport
 * ./gradlew koverVerify
 * ```
 */
plugins {
    id("org.jetbrains.kotlinx.kover")
}

kover {
    reports {
        // Configure verification rules
        verify {
            // Minimum line coverage: 80%
            rule {
                minBound(80)
            }
        }

        // Configure filters for all reports
        filters {
            // Exclude test code from coverage
            excludes {
                // Exclude test classes
                classes(
                    "*Test",
                    "*Test\$*",
                    "*Tests",
                    "*Tests\$*",
                    "*Spec",
                    "*Spec\$*"
                )

                // Exclude test packages
                packages(
                    "*.test",
                    "*.test.*",
                    "*.testing",
                    "*.testing.*"
                )
            }
        }
    }
}

// Add convenience tasks
tasks.register("coverageReport") {
    group = "verification"
    description = "Generates HTML and XML coverage reports"

    dependsOn("koverHtmlReport", "koverXmlReport")
}

tasks.register("coverageCheck") {
    group = "verification"
    description = "Verifies code coverage meets minimum thresholds"

    dependsOn("koverVerify")
}
