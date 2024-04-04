pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        maven("https://jitpack.io")
        maven("https://maven.aliucord.com/snapshots")
    }
}

include(":DHCord")
include(":Injector")
rootProject.name = "DHCord"
