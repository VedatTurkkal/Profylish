pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Profylish"
include(":app")
include(":core:ui")
include(":core:database")
include(":core:data")
include(":core:navigation")
include(":core:datastore")
include(":core:model")
include(":core:domain")
include(":core:network")
include(":core:analytics")
include(":core:common")
include(":feature:auth")
include(":feature:onboarding")
include(":feature:home")
include(":feature:lesson")
include(":feature:shop")
include(":feature:profile")
include(":feature:leaderboard")
include(":feature:settings")
include(":build-logic")
