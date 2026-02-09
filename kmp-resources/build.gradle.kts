plugins {
    id("kmp.ui.full")
}

android {
    namespace = "com.edugo.kmp.resources"

    sourceSets["main"].res.srcDirs("src/androidMain/res")
}
