package me.neon.mail.germ.impl

import com.germ.germplugin.api.dynamic.gui.GermGuiCanvas
import com.germ.germplugin.api.dynamic.gui.GermGuiCheckbox
import com.germ.germplugin.api.dynamic.gui.GermGuiScreen
import com.germ.germplugin.api.dynamic.gui.GermGuiScroll
import me.neon.mail.data.IPlayerData
import me.neon.mail.germ.GermPluginLoader
import me.neon.mail.mail.IMail
import me.neon.mail.mail.MailState
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

/**
 * NeonMail
 * me.neon.mail.germ.impl
 *
 * @author 老廖
 * @since 2024/3/8 2:22
 */
abstract class Screen(
    val viewer: Player,
    val viewerData: IPlayerData,
    guiName: String,
    section: ConfigurationSection
): GermGuiScreen(guiName, section) {

    protected lateinit var rightInfoCanvas: GermGuiCanvas

    protected lateinit var itemSlotCanvas: GermGuiCanvas

    protected lateinit var mailListCanvas: GermGuiCanvas

    protected lateinit var mailInfoCanvas: GermGuiCanvas

    protected var oldCheckbox: GermGuiCheckbox? = null

    abstract fun initRightInfo(mail: IMail<*>, isOpen: Boolean, infoCanvas: GermGuiCanvas)

    abstract fun initMailCanvasCheckBox()

    abstract fun initMailCanvas(mailState: MailState)

    protected fun init() {
        val main = getGuiPart("总画布", GermGuiCanvas::class.java)
        rightInfoCanvas = main.getGuiPart("右信息画布", GermGuiCanvas::class.java)
        // 在未选择前禁用状态
        rightInfoCanvas.isEnable = false

        // 右信息
        val itemList = rightInfoCanvas.getGuiPart("物品画布", GermGuiCanvas::class.java).getGuiPart("物品列表", GermGuiScroll::class.java)
        itemSlotCanvas = itemList.getGuiPart("物品槽子画布", GermGuiCanvas::class.java)!!.clone()
        itemList.removeGuiPart("物品槽子画布")

        // 处理左边
        mailListCanvas = main.getGuiPart("邮件列表画布", GermGuiCanvas::class.java)
        val mailList = mailListCanvas.getGuiPart("邮件列表滚动", GermGuiScroll::class.java)
        mailInfoCanvas = mailList.getGuiPart("邮件预览信息", GermGuiCanvas::class.java)!!.clone()
        mailList.removeGuiPart("邮件预览信息")

        // 初始化分类复选框
        initMailCanvasCheckBox()

        // 初始化头部按钮
        initTopButton()
    }

    open fun initTopButton() {
        val main = getGuiPart("总画布", GermGuiCanvas::class.java)
        main.getGuiPart("功能按钮画布", GermGuiCanvas::class.java)?.let {
            it.getGuiPart("草稿箱按钮", GermGuiCheckbox::class.java)?.let { a ->
                a.registerCallbackHandler({ _, _ ->
                    if (this is EditeScreen) return@registerCallbackHandler
                    EditeScreen(viewer, viewerData, GermPluginLoader.editeSection)
                }, GermGuiCheckbox.EventType.LEFT_CLICK)
            }
            it.getGuiPart("收件箱按钮", GermGuiCheckbox::class.java)?.let { a ->
                a.registerCallbackHandler({ _, _ ->
                    ReceiveScreen(viewer, viewerData, GermPluginLoader.receiveSection)
                }, GermGuiCheckbox.EventType.LEFT_CLICK)
            }
            it.getGuiPart("发件箱按钮", GermGuiCheckbox::class.java)?.let { a ->
                a.registerCallbackHandler({ _, _ ->
                    if (this is SenderScreen) return@registerCallbackHandler
                    SenderScreen(viewer, viewerData, GermPluginLoader.senderSection)
                }, GermGuiCheckbox.EventType.LEFT_CLICK)
            }
        }

    }

}