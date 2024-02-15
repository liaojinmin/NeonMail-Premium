package me.neon.mail.libs.taboolib.command

import me.neon.mail.libs.NeonLibsLoader
import me.neon.mail.libs.taboolib.command.component.CommandBase
import me.neon.mail.libs.taboolib.command.component.CommandComponent
import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ReflexClass


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandHeader(
    val name: String,
    val aliases: Array<String> = [],
    val description: String = "",
    val usage: String = "",
    val permission: String = "",
    val permissionMessage: String = "",
    val permissionDefault: PermissionDefault = PermissionDefault.OP,
    val newParser: Boolean = false,
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandBody(
    val aliases: Array<String> = [],
    val optional: Boolean = false,
    val permission: String = "",
    val permissionDefault: PermissionDefault = PermissionDefault.OP,
    val hidden: Boolean = false,
)

fun mainCommand(func: CommandBase.() -> Unit): SimpleCommandMain {
    return SimpleCommandMain(func)
}

fun subCommand(func: CommandComponent.() -> Unit): SimpleCommandBody {
    return SimpleCommandBody(func)
}

class SimpleCommandMain(val func: CommandBase.() -> Unit = {})

class SimpleCommandBody(val func: CommandComponent.() -> Unit = {}) {

    var name = ""
    var aliases = emptyArray<String>()
    var optional = false
    var permission = ""
    var permissionDefault: PermissionDefault = PermissionDefault.OP
    var hidden = false
    val children = ArrayList<SimpleCommandBody>()

    override fun toString(): String {
        return "SimpleCommandBody(name='$name', children=$children)"
    }
}

@Suppress("DuplicatedCode")
class SimpleCommandRegister {

    private val main = HashMap<String, SimpleCommandMain>()
    private val body = HashMap<String, MutableList<SimpleCommandBody>>()

    fun visit(clazz: Class<*>) {
       val i = clazz.getInstance(false)
        ReflexClass.of(clazz).structure.fields.forEach {
            visit2(it, clazz, i)
        }
        visitEnd(clazz)
    }

    private fun visit2(field: ClassField, clazz: Class<*>, instance: Any?) {
        if (field.isAnnotationPresent(CommandBody::class.java) && field.fieldType == SimpleCommandMain::class.java) {
            main[clazz.name] = field.get(instance) as SimpleCommandMain
        } else {
            body.computeIfAbsent(clazz.name) { ArrayList() } += loadBody(field, instance) ?: return
        }
    }

    private fun loadBody(field: ClassField, instance: Any?): SimpleCommandBody? {
        if (field.isAnnotationPresent(CommandBody::class.java)) {
            val annotation = field.getAnnotation(CommandBody::class.java)
            val obj = field.get(instance)
            return when (field.fieldType) {
                SimpleCommandMain::class.java -> {
                    null
                }
                SimpleCommandBody::class.java -> {
                    (obj as SimpleCommandBody).apply {
                        name = field.name
                        aliases = annotation.property("aliases", emptyArray())
                        optional = annotation.property("optional", false)
                        permission = annotation.property("permission", "")
                        permissionDefault = annotation.enum("permissionDefault", PermissionDefault.OP)
                        hidden = annotation.property("hidden", false)
                    }
                }
                else -> {
                    SimpleCommandBody().apply {
                        name = field.name
                        aliases = annotation.property("aliases", emptyArray())
                        optional = annotation.property("optional", false)
                        permission = annotation.property("permission", "")
                        permissionDefault = annotation.enum("permissionDefault", PermissionDefault.OP)
                        hidden = annotation.property("hidden", false)
                        ReflexClass.of(field.fieldType).structure.fields.forEach {
                            children += loadBody(it, instance) ?: return@forEach
                        }
                    }
                }
            }
        }
        return null
    }

    private fun visitEnd(clazz: Class<*>) {
        if (clazz.isAnnotationPresent(CommandHeader::class.java)) {
            val annotation = clazz.getAnnotation(CommandHeader::class.java)
            command(
                annotation.name,
                annotation.aliases.toList(),
                annotation.description,
                annotation.usage,
                annotation.permission,
                annotation.permissionMessage,
                annotation.permissionDefault,
                body[clazz.name]?.filter { it.permission.isNotEmpty() }
                    ?.associate { it.permission to it.permissionDefault } ?: emptyMap(),
                annotation.newParser,
            ) {
                main[clazz.name]?.func?.invoke(this)
                body[clazz.name]?.forEach { body ->
                    fun register(body: SimpleCommandBody, component: CommandComponent) {
                        component.literal(body.name, *body.aliases, optional = body.optional, permission = body.permission, hidden = body.hidden) {
                            if (body.children.isEmpty()) {
                                body.func(this)
                            } else {
                                body.children.forEach { children ->
                                    register(children, this)
                                }
                            }
                        }
                    }
                    register(body, this)
                }
            }
        }
    }

    private fun <T> Class<T>.getInstance(newInstance: Boolean = false): T? {
        return try {
            val field = if (simpleName == "Companion") {
                // 获取 Kotlin Companion 字段
                ReflexClass.of(getClass(name.substringBeforeLast('$'))).getField("Companion", findToParent = false, remap = false)
            } else {
                // 获取 Kotlin Object 字段
                ReflexClass.of(this).getField("INSTANCE", findToParent = false, remap = false)
            }
            field.get() as T
        } catch (ex: NoSuchFieldException) {
            // 是否创建实例
            if (newInstance) getDeclaredConstructor().newInstance() as T else null
        } catch (ex: NoClassDefFoundError) {
            null
        } catch (ex: ClassNotFoundException) {
            null
        } catch (ex: IllegalAccessError) {
            null
        } catch (ex: IncompatibleClassChangeError) {
            null
        } catch (ex: ExceptionInInitializerError) {
            println(this)
            ex.printStackTrace()
            null
        } catch (ex: InternalError) {
            if (ex.message != "Malformed class name") {
                println(this)
                ex.printStackTrace()
            }
            null
        }
    }

    private fun getClass(name: String): Class<*> {
        return Class.forName(name, false, NeonLibsLoader::class.java.classLoader)
    }

}