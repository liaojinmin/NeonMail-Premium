package me.neon.mail.menu


import me.neon.mail.utils.forFile
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.file.YamlConfiguration
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.warning
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.platform.util.bukkitPlugin
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

/**
 * NeonMail-Premium
 * me.neon.mail.menu
 *
 * @author 老廖
 * @since 2024/1/4 13:56
 */
object MenuLoader {

    private val menuClickCd: ConcurrentHashMap<UUID, Long> = ConcurrentHashMap()

    private val typeMap: Map<String, MenuType> by lazy {
        hashMapOf(
            "sender" to MenuType.Sender,
            "receive" to MenuType.Receive,
            "actions" to MenuType.Actions,
            "itemPreview" to MenuType.ItemPreview,
            "draftBox" to MenuType.DraftBox,
            "typeSelect" to MenuType.TypeSelect,
            "draftMailEdite" to MenuType.DraftMailEdite,
            "mailAppEdite" to MenuType.MailAppEdite,
            "playerList" to MenuType.PlayerList,
            "itemEdite" to MenuType.ItemEdite,
        )
    }

    private var menuMap: MutableMap<MenuType, MenuData> = mutableMapOf()

    fun addClickCD(uuid: UUID, cd: Int) {
        if (cd <= 0) return
        menuClickCd[uuid] = System.currentTimeMillis() + (cd * 1000)
    }

    fun isClickCD(uuid: UUID): Boolean {
        return (menuClickCd[uuid] ?: 0) >= System.currentTimeMillis()
    }


    fun getMenuData(type: MenuType): MenuData {
        return menuMap[type] ?: error("找不到种类 $type 的菜单配置....")
    }


    @Awake(LifeCycle.ENABLE)
    fun loaderMenu() {
        measureTimeMillis {
            val file = File(getDataFolder(), "menu")
            if (!file.exists()) {
                // create
                createNewFile()
            }
            file.forFile("yml").forEach {
                val name = it.name.substringBeforeLast(".")
                typeMap[name]?.let { type ->
                    menuMap[type] = loadMenu(YamlConfiguration.loadConfiguration(it), it)
                }
            }
        }.also { Bukkit.getConsoleSender().sendMessage("§7[NeonMail] §7加载 §f${menuMap.size} §7个菜单配置文件 §7... §8(耗时 $it ms)") }
    }


    private fun loadMenu(yaml: Configuration, file: File): MenuData {
        val title = yaml.getString("title")?.colored() ?: "menu"
        val layout = yaml.getStringList("layout")
        val icon = mutableMapOf<Char, MenuIcon>()
        try {
            yaml.getConfigurationSection("icons")?.let {
                it.getKeys(false).forEach { key ->
                    val ic = MenuIcon(
                        key.first(),
                        XMaterial.matchXMaterial(it.getString("$key.display.mats")?.uppercase() ?: "PAPER").get().parseMaterial() ?: Material.STONE,
                        it.getInt("$key.display.model"),
                        it.getString("$key.display.name")?.colored() ?: " ",
                        it.getStringList("$key.display.lore").colored(),
                        it.getString("$key.display.action") ?: ""
                    )
                    it.getConfigurationSection("$key.display.subIcon")?.let { cs ->
                        ic.subIcon = MenuIcon(
                            ic.char,
                            Material.valueOf(cs.getString("mats") ?: "PAPER"),
                            cs.getInt("model"),
                            cs.getString("name")?.colored() ?: " ",
                            cs.getStringList("lore").colored(),
                            cs.getString("action") ?: ""
                        )
                    }
                    icon[key.first()] = ic
                }
            }
        } catch (e: Exception) {
            warning("目录: ${file.name}")
            warning("出错文件 -> ${yaml.name}")
            e.printStackTrace()
        }
        return MenuData(title, layout.toTypedArray(), icon)
    }


    private fun createNewFile() {
        listOf(
            "menu/sender.yml",
            "menu/receive.yml",
            "menu/actions.yml",
            "menu/itemPreview.yml",
            "menu/edits/draftBox.yml",
            "menu/edits/typeSelect.yml",
            "menu/edits/draftMailEdite.yml",
            "menu/edits/mailAppEdite.yml",
            "menu/edits/playerList.yml",
            "menu/edits/itemEdite.yml"
        ).forEach {
            bukkitPlugin.saveResource(it, true)
        }
    }

}