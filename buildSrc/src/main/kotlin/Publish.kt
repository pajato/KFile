import java.io.File
import java.util.Properties

object Publish {
    const val GROUP = "com.pajato.io"
    const val POM_DESCRIPTION = "Kotlin Multiplatform File"
    const val POM_DEVELOPER_ID = "pajatopmr"
    const val POM_DEVELOPER_NAME = "Paul Michael Reilly"
    const val POM_ORGANIZATION_NAME = "Pajato Technologies LLC"
    const val POM_ORGANIZATION_URL = "https://pajato.com/"
    const val POM_NAME = "KFile"
    const val POM_LICENSE_DIST = "repo"
    const val POM_LICENSE_NAME = "The GNU Lesser General Public License, Version 3"
    const val POM_LICENSE_URL = "https://www.gnu.org/copyleft/lesser.html"
    const val POM_SCM_CONNECTION = "scm:git:https://github.com/pajato/KFile.git"
    const val POM_SCM_DEV_CONNECTION = "scm:git:git@github.com:pajato/KFile.git"
    const val POM_SCM_URL = "https://github.com/pajato/KFile"
    const val POM_URL = "https://github.com/pajato/KFile"

    private var properties = Properties()

    fun getProperty(dir: String, name: String): String {
        fun loadProperties() {
            val propertyFile = File(dir, "local.properties")
            properties.apply { propertyFile.inputStream().use { load(it) } }
        }

        if (properties.size == 0) loadProperties()
        return if (properties.containsKey(name)) properties.getProperty(name) else ""
    }
}
