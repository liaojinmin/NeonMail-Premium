package me.neon.mail.germ.impl

import com.germ.germplugin.api.dynamic.gui.GermGuiButton
import com.germ.germplugin.api.dynamic.gui.GermGuiCanvas
import com.germ.germplugin.api.dynamic.gui.GermGuiCheckbox
import com.germ.germplugin.api.dynamic.gui.GermGuiItem
import com.germ.germplugin.api.dynamic.gui.GermGuiLabel
import com.germ.germplugin.api.dynamic.gui.GermGuiScreen
import com.germ.germplugin.api.dynamic.gui.GermGuiScroll
import me.neon.mail.Settings
import me.neon.mail.data.IPlayerData
import me.neon.mail.mail.*
import me.neon.mail.service.ServiceManager.deleteMail
import me.neon.mail.service.ServiceManager.updateState
import me.neon.mail.utils.asyncRunner
import me.neon.mail.utils.syncRunner
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import me.neon.mail.Settings.sendLang

/**
 * NeonMail
 * me.neon.mail.germ.impl
 *
 * @author 老廖
 * @since 2024/3/6 10:06
 */
class ReceiveScreen(
    viewer: Player,
    viewerData: IPlayerData,
    section: ConfigurationSection
): Screen(viewer, viewerData, "收件箱-UI", section) {

    private var nowStats: MailState = MailState.Text

    private var isClose: Boolean = false

    init {
        init()
        val main = getGuiPart("总画布", GermGuiCanvas::class.java)
        main.getGuiPart("功能按钮画布", GermGuiCanvas::class.java)?.let {
            it.getGuiPart("收件箱按钮", GermGuiCheckbox::class.java)?.checked = true
            it.getGuiPart("草稿箱按钮", GermGuiCheckbox::class.java)?.checked = false
            it.getGuiPart("发件箱按钮", GermGuiCheckbox::class.java)?.checked = false
        }
        setClosedHandler { _, _ ->
            isClose = true
        }
        openGui(viewer)
        initMailCanvas(MailState.Text)
    }

    override fun initRightInfo(mail: IMail<*>, isOpen: Boolean, infoCanvas: GermGuiCanvas) {
        if (isOpen) {
            rightInfoCanvas.isEnable = true
        } else {
            rightInfoCanvas.isEnable = false
            return
        }
        rightInfoCanvas.getGuiPart("领取附件", GermGuiButton::class.java).apply {
            if (!isEnable) isEnable = true
            registerCallbackHandler({ _, _ ->
                if (mail.state == MailState.NotObtained) {
                    if (mail.data.isSuccessAppendix(viewer)) {
                        mail.state = MailState.Acquired
                        mail.collectTimer = System.currentTimeMillis()
                        listOf(mail).updateState {
                            // 数据库更新成功才发放奖励
                            syncRunner {
                                mail.data.giveAppendix(viewer)
                                viewer.sendLang(
                                    "玩家-领取附件-成功",
                                    mail.data.getAllAppendixInfo(viewer)
                                )
                                // 更新图标，如果有的话
                                infoCanvas.getGuiPart("邮件图标", GermGuiItem::class.java).also { item ->
                                    if (item.isEnable) {
                                        item.itemStack = mail.parseMailIcon(viewer, item.itemStack)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    viewer.sendLang("邮件-领取附件-失败")
                }
            }, GermGuiButton.EventType.LEFT_CLICK)
        }
        rightInfoCanvas.getGuiPart("删除邮件", GermGuiButton::class.java).apply {
            if (!isEnable) isEnable = true
            registerCallbackHandler({ _, _ ->
                if (mail.state == MailState.Acquired || mail.state == MailState.Text) {
                    if (viewerData.receiveBox.removeIf { it.unique == mail.unique }) {
                        mail.deleteMail(viewer.uniqueId == mail.sender)
                        viewer.sendLang("邮件-删除操作-成功", 1)
                        initMailCanvas(nowStats)
                    }
                } else {
                    viewer.sendLang("邮件-删除操作-失败-附件存在")
                }
            }, GermGuiButton.EventType.LEFT_CLICK)
        }
        rightInfoCanvas.getGuiPart("回复邮件", GermGuiButton::class.java).apply {
            if (!isEnable) isEnable = true
            registerCallbackHandler({ _, _ ->
                // TODO()
            }, ClickType.LEFT_CLICK)
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
                    mail.data.getItemAppendix()?.forEachIndexed { index, item ->
                val itemCanvas = itemSlotCanvas.clone()
                        itemCanvas.getGuiPart("物品附件", GermGuiItem::class.java).also { ui ->
                            ui.itemStack = item
                        }
                        itemCanvas.indexName = itemCanvas.indexName + index
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
        mailListCanvas.getGuiPart("未读邮件", GermGuiCheckbox::class.java).apply {
            if (!isEnable) isEnable = true

            this.registerCallbackHandler({ _, _ ->
                if (!(checked as Boolean)) {
                    //取消选中
                    oldCheckbox?.checked = false
                    // 关闭右侧信息
                    rightInfoCanvas.isEnable = false
                    // 将列表邮件设置未未读类型
                    initMailCanvas(MailState.NotObtained)
                    nowStats = MailState.NotObtained
                    mailListCanvas.getGuiPart("全部邮件", GermGuiCheckbox::class.java).isChecked = false
                    mailListCanvas.getGuiPart("已读邮件", GermGuiCheckbox::class.java).isChecked = false
                }
            }, GermGuiCheckbox.EventType.LEFT_CLICK)
        }

        mailListCanvas.getGuiPart("已读邮件", GermGuiCheckbox::class.java).apply {
            if (!isEnable) isEnable = true
            registerCallbackHandler({ _, _ ->
                if (!(checked as Boolean)) {
                    //取消选中
                    oldCheckbox?.checked = false
                    // 关闭右侧信息
                    rightInfoCanvas.isEnable = false
                    // 将列表邮件设置未已读类型
                    initMailCanvas(MailState.Acquired)
                    nowStats = MailState.Acquired
                    mailListCanvas.getGuiPart("全部邮件", GermGuiCheckbox::class.java).isChecked = false
                    mailListCanvas.getGuiPart("未读邮件", GermGuiCheckbox::class.java).isChecked = false
                }
            }, GermGuiCheckbox.EventType.LEFT_CLICK)
        }

        mailListCanvas.getGuiPart("全部邮件", GermGuiCheckbox::class.java).apply {
            if (!isEnable) isEnable = true
            registerCallbackHandler({ _, _ ->
                if (!(checked as Boolean)) {
                    //取消选中
                    oldCheckbox?.checked = false
                    // 关闭右侧信息
                    rightInfoCanvas.isEnable = false
                    // 将列表邮件设置未已读类型
                    initMailCanvas(MailState.Text)
                    nowStats = MailState.Text
                    mailListCanvas.getGuiPart("已读邮件", GermGuiCheckbox::class.java).isChecked = false
                    mailListCanvas.getGuiPart("未读邮件", GermGuiCheckbox::class.java).isChecked = false
                }
            }, GermGuiCheckbox.EventType.LEFT_CLICK)
        }
    }

    override fun initMailCanvas(mailState: MailState) {
        mailListCanvas.getGuiPart("邮件列表滚动", GermGuiScroll::class.java).apply {
            clearGuiPart()
            //asyncRunner {
                for (it in viewerData.receiveBox) {
                    if (isClose) return

                    if (it.state == mailState || (it.state == MailState.Text || mailState == MailState.Text)) {
                        val mail = mailInfoCanvas.clone()
                        if (!mail.isEnable) mail.isEnable = true

                        mail.getGuiPart("邮件复选框", GermGuiCheckbox::class.java).also { check ->
                            check.registerCallbackHandler({ _, e ->
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
                        mail.getGuiPart("发件人", GermGuiLabel::class.java).also { label ->
                            if (label.isEnable) {
                                label.setText(
                                    MailUtils.replacements[MailUtils.SENDER]?.invoke(it) ?: "无法获取发送者"
                                )
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
          //  }
        }

    }



}