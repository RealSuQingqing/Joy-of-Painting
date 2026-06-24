plugins {
    id("dev.prism")
}

group = "com.leclowndu93150"
version = "1.0.1"

prism {
    metadata {
        modId = "joyofpainting"
        name = "Joy of Painting"
        description = "Adds canvases and easels for painting."
        license = "GPL-3.0"
        author("xerca")
        author("Leclowndu93150")
    }

    curseMaven()

    publishing {

        changelog = "fix server crash"

        curseforge {
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            projectId = "1581050"
        }
    }

    version("26.2") {
        neoforge {
            loaderVersion = "26.2.0.7-beta"
            loaderVersionRange = "[4,)"
            mixins {
                config("joyofpainting.mixins.json")
                refmap("joyofpainting.refmap.json")
            }

            dependencies {
                modRuntimeOnly("curse.maven:jei-238222:8108851")
            }
        }
    }

    version("26.1.2") {
        neoforge {
            loaderVersion = "26.1.2.76"
            loaderVersionRange = "[4,)"
            mixins {
                config("joyofpainting.mixins.json")
                refmap("joyofpainting.refmap.json")
            }

            dependencies {
                modRuntimeOnly("curse.maven:jei-238222:8108851")
            }
        }
    }

    version("1.21.1") {
        neoforge {
            loaderVersion = "21.1.233"
            loaderVersionRange = "[4,)"
            mixins {
                config("joyofpainting.mixins.json")
                refmap("joyofpainting.refmap.json")
            }

            dependencies {
                modRuntimeOnly("curse.maven:jei-238222:7391682")
            }
        }
    }

    version("1.20.1") {
        forge {
            loaderVersion = "47.4.20"
            loaderVersionRange = "[47,)"
            mixins {
                config("joyofpainting.mixins.json")
                refmap("joyofpainting.refmap.json")
            }

            dependencies {
                modRuntimeOnly("curse.maven:jei-238222:7920915")
            }
        }
    }

}
