package java.io

class File {
    val path: String
    val name: String
    val parentFile: File?
    val absolutePath: String get() = path

    constructor(pathname: String) {
        this.path = pathname
        this.name = pathname.substringAfterLast('/').substringAfterLast('\\')
        val parentPath = pathname.substringBeforeLast('/', "").ifEmpty { pathname.substringBeforeLast('\\', "") }
        this.parentFile = if (parentPath.isNotEmpty()) File(parentPath) else null
    }

    constructor(parent: String?, child: String) {
        this.path = if (parent.isNullOrEmpty()) child else "$parent/$child"
        this.name = child.substringAfterLast('/').substringAfterLast('\\')
        this.parentFile = if (!parent.isNullOrEmpty()) File(parent) else null
    }

    constructor(parent: File?, child: String) {
        this.path = if (parent == null || parent.path.isEmpty()) child else "${parent.path}/$child"
        this.name = child.substringAfterLast('/').substringAfterLast('\\')
        this.parentFile = parent
    }

    fun mkdirs(): Boolean = true

    fun writeText(text: String, charset: Any? = null) {
        println("[WebFile] Simulação de gravação em $path (${text.length} caracteres)")
    }

    override fun toString(): String = path
    override fun equals(other: Any?): Boolean = other is File && other.path == path
    override fun hashCode(): Int = path.hashCode()
}
