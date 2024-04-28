package me.neon.mail.germ

import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.platform.util.bukkitPlugin
import java.io.File

/**
 * NeonMail
 * me.neon.mail.germ
 *
 * @author 老廖
 * @since 2024/3/5 5:12
 */
object GermPluginLoader {


    private val screenFile: File by lazy {
        File(getDataFolder(), "germ")
    }

    lateinit var receiveSection: ConfigurationSection
        private set

    lateinit var senderSection: ConfigurationSection
        private set

    lateinit var editeSection: ConfigurationSection
        private set

    lateinit var playerSection: ConfigurationSection
        private set

    lateinit var itemsSection: ConfigurationSection
        private set

    @Awake(LifeCycle.ENABLE)
    fun loader() {
        if (!screenFile.exists()) {
            createNewFile()
        }
        runCatching {
            if (Bukkit.getPluginManager().getPlugin("GermPlugin") != null) {
                receiveSection =
                    com.germ.germplugin.api.yaml.YamlRoot.getYamlRoot(File(screenFile, "receive.yml"))
                        .getConfigurationSection("收件箱-UI")!!
                senderSection =
                    com.germ.germplugin.api.yaml.YamlRoot.getYamlRoot(File(screenFile, "sender.yml"))
                        .getConfigurationSection("发件箱-UI")!!
                editeSection =
                    com.germ.germplugin.api.yaml.YamlRoot.getYamlRoot(File(screenFile, "edits.yml"))
                        .getConfigurationSection("草稿箱-UI")!!
                playerSection =
                    com.germ.germplugin.api.yaml.YamlRoot.getYamlRoot(File(screenFile, "edits_player.yml"))
                        .getConfigurationSection("草稿箱-选择玩家UI")!!
                itemsSection =
                    com.germ.germplugin.api.yaml.YamlRoot.getYamlRoot(File(screenFile, "edits_item.yml"))
                        .getConfigurationSection("草稿箱-物品子UI")!!

            }
        }
    }


    private fun createNewFile() {
        listOf(
            "germ/receive.yml",
            "germ/sender.yml",
            "germ/edits.yml",
            "germ/edits_player.yml",
            "germ/edits_item.yml",
        ).forEach {
            bukkitPlugin.saveResource(it, true)
        }
    }

}