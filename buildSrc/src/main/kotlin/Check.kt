import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.io.File

private val constantUtf8 = ClassReader::class.java.getDeclaredField("constantUtf8Values")
    .also { it.trySetAccessible() }

@Throws(RuntimeException::class)
fun check(file: File) {
    if (file.extension != "class") return
    val classReader = ClassReader(file.readBytes())
    if (classReader.interfaces?.contains("org/spongepowered/asm/mixin/extensibility/IMixinConfigPlugin") ?: false) {
        val classNode = ClassNode(Opcodes.ASM9)
        classReader.accept(classNode, 0)
        @Suppress("UNCHECKED_CAST") val strings = (constantUtf8.get(classReader) as Array<String>).filterNotNull()
        val res = strings.filter { it.contains(Regex("""net[\.\/]minecraft""")) }
        if (res.isNotEmpty()) {
            val name = classReader.className
            throw RuntimeException("reference mc stuff in $name\n${res.joinToString(separator = "\n")}")
        }
    }
}