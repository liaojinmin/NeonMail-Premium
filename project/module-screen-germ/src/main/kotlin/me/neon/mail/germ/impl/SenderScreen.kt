package me.neon.mail.germ.impl

import com.germ.germplugin.api.dynamic.gui.GermGuiButton
import com.germ.germplugin.api.dynamic.gui.GermGuiCanvas
import com.germ.germplugin.api.dynamic.gui.GermGuiCheckbox
import com.germ.germplugin.api.dynamic.gui.GermGuiItem
import com.germ.germplugin.api.dynamic.gui.GermGuiLabel
import com.germ.germplugin.api.dynamic.gui.GermGuiScroll
import me.neon.mail.data.IPlayerData
import me.neon.mail.germ.GermPluginLoader
import me.neon.mail.mail.*
import me.neon.mail.service.ServiceManager.deleteMails
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import taboolib.platform.util.cancelNextChat
import taboolib.platform.util.nextChatInTick
import me.neon.mail.Settings.sendLang

/**
 * NeonMail
 * me.neon.mail.germ.impl
 *
 * @author 老廖
 * @since 2024/3/6 10:06
 */
class SenderScreen(
    viewer: Player,
    viewerData: IPlayerData,
    section: ConfigurationSection
): Screen(viewer, viewerData, "发件箱-UI", section) {

    init {
        init()
        val main = getGuiPart("总画布", GermGuiCanvas::class.java)
        main.getGuiPart("功能按钮画布", GermGuiCanvas::class.java)?.let {
            it.getGuiPart("收件箱按钮", GermGuiCheckbox::class.java)?.checked = false
            it.getGuiPart("草稿箱按钮", GermGuiCheckbox::class.java)?.checked = false
            it.getGuiPart("发件箱按钮", GermGuiCheckbox::class.java)?.checked = true
        }
        openGui(viewer)
        // 初始化邮件列表
        initMailCanvas(MailState.Text)
    }

    override fun initRightInfo(mail: IMail<*>, isOpen: Boolean, infoCanvas: GermGuiCanvas) {
        if (isOpen) rightInfoCanvas.isEnable = true
        else {
            rightInfoCanvas.isEnable = false
            return
        }
        rightInfoCanvas.getGuiPart("删除邮件", GermGuiButton::class.java).apply {
            if (!isEnable) isEnable = true
            registerCallbackHandler({ _, _ ->
                if (viewerData.senderBox.removeIf { it.unique == mail.unique }) {
                    listOf(mail).deleteMails(true)
                    viewer.sendLang("邮件-删除操作-成功", 1)
                    initMailCanvas(MailState.Text)
                }
            }, GermGuiButton.EventType.LEFT_CLICK)
        }

        rightInfoCanvas.getGuiPart("信息画布", GermGuiCanvas::class.java).apply {
            getGuiPart("标题", GermGuiLabel::class.java).also {
                if (it.isEnable) {
                    it.setText(mail.title)
                }
            }
            getGuiPart("文本列表滚动", GermGuiScroll::class.java).also { scroll ->
                if (scroll.isEnable) {
                    scroll.getGuiPart("文本", GermGuiLabel::class.java)?.let { label ->
                        if (label.isEnable)
                            label.texts = mail.context.split(";")
                    }
                }
            }
        }

        // 更新物品附件列表
        if (mail.data.hasItemAppendix()) {
            rightInfoCanvas.getGuiPart("物品画布", GermGuiCanvas::class.java)
                .getGuiPart("物品列表", GermGuiScroll::class.java).apply {
                    mail.data.getItemAppendix()?.forEach {
                        val itemCanvas = itemSlotCanvas.clone()
                        itemCanvas.getGuiPart("物品附件", GermGuiItem::class.java).also { ui ->
                            ui.itemStack = it
                        }
                        addGuiPart(itemCanvas)
                    }
                }
        }

        // 更新可被文本解析的附件
        rightInfoCanvas.getGuiPart("货币画布", GermGuiCanvas::class.java).apply {
            getGuiParts(GermGuiLabel::class.java).forEach {
                it.setText(MailRegister.getAppendixInfoParse(it.indexName, mail))
            }
        }
    }

    override fun initMailCanvasCheckBox() {
        mailListCanvas.getGuiPart("删除全部", GermGuiCheckbox::class.java).apply {
            if (!isEnable) isEnable = true
            registerCallbackHandler({ _, _ ->
                if (!(checked as Boolean)) {
                    //取消选中
                    oldCheckbox?.checked = false
                    // 删除所有已读
                    if (viewerData.senderBox.size > 0) {
                        this@SenderScreen.close()
                        viewer.sendLang("邮件-删除操作-确认")
                        viewer.nextChatInTick(400, {
                            if (it.equals("cancel", ignoreCase = true)) {
                                viewer.cancelNextChat(false)
                            } else if (it == "确认" || it == "ok") {
                                viewer.sendLang("邮件-删除操作-成功", viewerData.senderBox.size)
                                // 复制
                                val list = viewerData.senderBox.toList()
                                // 更新
                                list.deleteMails(true)
                                // 清理
                                viewerData.senderBox.clear()
                                //重新打开
                                SenderScreen(viewer, viewerData, GermPluginLoader.senderSection)
                            }
                        })
                    }
                }
            }, GermGuiCheckbox.EventType.LEFT_CLICK)
        }
    }

    override fun initMailCanvas(mailState: MailState) {
        mailListCanvas.getGuiPart("邮件列表滚动", GermGuiScroll::class.java).apply {
            clearGuiPart()
            viewerData.senderBox.forEach {
                val mail = mailInfoCanvas.clone()
                if (!mail.isEnable) mail.isEnable = true
                mail.getGuiPart("邮件复选框", GermGuiCheckbox::class.java).also { check ->
                    check.registerCallbackHandler({ _, _ ->
                        val ac = !(check.checked as Boolean)
                        oldCheckbox?.isChecked = false
                        oldCheckbox = check
                        if (ac) check.isChecked = true
                        initRightInfo(it, ac, mail)
                    }, GermGuiCheckbox.EventType.LEFT_CLICK)
                }
                mail.getGuiPart("邮件图标", GermGuiItem::class.java).also { item ->
                    if (item.isEnable) {
                        item.itemStack = it.parseMailIcon(viewer, item.itemStack)
                    }
                }
                mail.getGuiPart("标题", GermGuiLabel::class.java).also { label ->
                    if (label.isEnable) {
                        label.setText(it.title)
                    }
                }
                mail.getGuiPart("发件时间", GermGuiLabel::class.java).also { label ->
                    if (label.isEnable) {
                        label.setText(it.parseMailSenderTimer())
                    }
                }
                mail.indexName = it.unique.toString()
                addGuiPart(mail)

            }
        }
    }
}