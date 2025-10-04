import com.android.build.gradle.LibraryExtension
import de.mindmarket.chirp.convention.configureKotlinAndroid
import de.mindmarket.chirp.convention.configureKotlinMultiplatform
import de.mindmarket.chirp.convention.libs
import de.mindmarket.chirp.convention.pathToResourcePrefix
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class KmpLibraryConventionPlugin: Plugin<Project>{
    override fun apply(target: Project) {
       with(target) {
           with(pluginManager) {
               apply("com.android.library")
               apply("org.jetbrains.kotlin.multiplatform")
               apply("org.jetbrains.kotlin.plugin.serialization")
           }

           configureKotlinMultiplatform()

           extensions.configure<LibraryExtension> {
               configureKotlinAndroid(this)

               resourcePrefix = this@with.pathToResourcePrefix()

               // required to make debug build of app run in iOS simulator
               experimentalProperties["android.experimental.kmp.enableAndroidResources"] = "true"
           }

           dependencies {
               "commonMainImplementation"(libs.findLibrary("kotlinx-serialization-json").get())
               "commonMainImplementation"(libs.findLibrary("kotlin-test").get())
           }
       }
    }
}