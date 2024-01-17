package me.neon.mail.menu

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.console
import taboolib.common.platform.function.warning
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
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
@PlatformSide([Platform.BUKKIT])
object MenuLoader {

    private val menuClickCd: ConcurrentHashMap<UUID, Long> = ConcurrentHashMap()

    fun addClickCD(uuid: UUID, cd: Int) {
        if (cd <= 0) return
        menuClickCd[uuid] = System.currentTimeMillis() + (cd * 1000)
    }
    fun isClickCD(uuid: UUID): Boolean {
        return (menuClickCd[uuid] ?: 0) >= System.currentTimeMillis()
    }

    @Config(value = "menu/sender.yml", autoReload = true)
    private lateinit var sender: ConfigFile

    @Config(value = "menu/receive.yml", autoReload = true)
    private lateinit var receive: ConfigFile

    @Config(value = "menu/actions.yml", autoReload = true)
    private lateinit var actions: ConfigFile

    @Config(value = "menu/itemPreview.yml", autoReload = true)
    private lateinit var itemPreview: ConfigFile

    // edit
    @Config(value = "menu/edits/draftBox.yml", autoReload = true)
    private lateinit var draftBox: ConfigFile

    @Config(value = "menu/edits/typeSelect.yml", autoReload = true)
    private lateinit var typeSelect: ConfigFile

    @Config(value = "menu/edits/draftMailEdite.yml", autoReload = true)
    private lateinit var draftMailEdite: ConfigFile

    @Config(value = "menu/edits/mailAppEdite.yml", autoReload = true)
    private lateinit var mailAppEdite: ConfigFile

    @Config(value = "menu/edits/playerList.yml", autoReload = true)
    private lateinit var playerList: ConfigFile

    @Config(value = "menu/edits/itemEdite.yml", autoReload = true)
    private lateinit var itemEdite: ConfigFile

    lateinit var senderMenu: MenuData
        private set
    lateinit var receiveMenu: MenuData
        private set
    lateinit var actionsMenu: MenuData
        private set
    lateinit var itemPreviewMenu: MenuData
        private set

    // edit
    lateinit var draftBoxMenu: MenuData
        private set
    lateinit var typeSelectMenu: MenuData
        private set
    lateinit var draftMailEditeMenu: MenuData
        private set
    lateinit var mailAppEditeMenu: MenuData
        private set
    lateinit var playerListMenu: MenuData
        private set
    lateinit var itemEditeMenu: MenuData
        private set


    fun reload() {
        measureTimeMillis {
            senderMenu = loadMenu(sender)
            receiveMenu = loadMenu(receive)
            actionsMenu = loadMenu(actions)
            itemPreviewMenu = loadMenu(itemPreview)

            // edit
            draftBoxMenu = loadMenu(draftBox)
            typeSelectMenu = loadMenu(typeSelect)
            draftMailEditeMenu = loadMenu(draftMailEdite)
            mailAppEditeMenu = loadMenu(mailAppEdite)
            playerListMenu = loadMenu(playerList)
            itemEditeMenu = loadMenu(itemEdite)
        }.also { console().sendMessage("§7加载 §f菜单配置文件 §7... §8(耗时 $it ms)") }
    }


    @Awake(LifeCycle.ENABLE)
    private fun loader() {
        sender.onReload { senderMenu = loadMenu(sender) }
        receive.onReload { receiveMenu = loadMenu(receive) }
        actions.onReload { actionsMenu = loadMenu(actions) }
        itemPreview.onReload { itemPreviewMenu = loadMenu(itemPreview) }
        // edit
        draftBox.onReload { draftBoxMenu = loadMenu(draftBox) }
        typeSelect.onReload { typeSelectMenu = loadMenu(typeSelect) }
        draftMailEdite.onReload { draftMailEditeMenu = loadMenu(draftMailEdite) }
        mailAppEdite.onReload { mailAppEditeMenu = loadMenu(mailAppEdite) }
        playerList.onReload { playerListMenu = loadMenu(playerList) }
        itemEdite.onReload { itemEditeMenu = loadMenu(itemEdite) }
        reload()
    }


    private fun loadMenu(yaml: ConfigFile): MenuData {
        val title = yaml.getString("title")?.colored() ?: "menu"
        val layout = yaml.getStringList("layout")
        val icon = mutableMapOf<Char, MenuIcon>()
        try {


            yaml.getConfigurationSection("icons")?.let {
                it.getKeys(false).forEach { key ->
                    val ic = MenuIcon(
                        key.first(),
                        XMaterial.valueOf(it.getString("$key.display.mats")?.uppercase() ?: "PAPER"),
                        it.getInt("$key.display.model"),
                        it.getString("$key.display.name")?.colored() ?: " ",
                        it.getStringList("$key.display.lore").colored(),
                        it.getString("$key.display.action") ?: ""
                    )
                    it.getConfigurationSection("$key.display.subIcon")?.let { cs ->
                        ic.subIcon = MenuIcon(
                            ic.char,
                            XMaterial.valueOf(cs.getString("mats") ?: "PAPER"),
                            cs.getInt("model"),
                            cs.getString("name")?.colored() ?: " ",
                            cs.getStringList("lore").colored(),
                            cs.getString("action") ?: ""
                        )
                    }
                    icon[key.first()] = ic
                }
            }
        }catch (e: Exception) {
            e.printStackTrace()
            warning("出错文件 -> ${yaml.name}")
        }
        return MenuData(title, layout.toTypedArray(), icon)
    }
}