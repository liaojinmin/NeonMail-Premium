package me.neon.mail.germ.impl

import com.germ.germplugin.api.dynamic.gui.*
import me.neon.mail.NeonMailAPI
import me.neon.mail.Settings.sendLang
import me.neon.mail.data.IPlayerData
import me.neon.mail.germ.GermPluginLoader
import me.neon.mail.hook.ProviderRegister
import me.neon.mail.mail.*
import me.neon.mail.service.ServiceManager.deleteToSql
import me.neon.mail.service.ServiceManager.updateToSql
import me.neon.mail.utils.Heads
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.isNotAir
import java.util.UUID

/**
 * NeonMail
 * me.neon.mail.germ.impl
 *
 * @author 老廖
 * @since 2024/3/6 10:06
 */
class EditeScreen(
    viewer: Player,
    viewerData: IPlayerData,
    section: ConfigurationSection
): Screen(viewer, viewerData, "草稿箱-UI", section) {

    init {
        init()
        val main = getGuiPart("总画布", GermGuiCanvas::class.java)
        main.getGuiPart("功能按钮画布", GermGuiCanvas::class.java)?.let {
            it.getGuiPart("收件箱按钮", GermGuiCheckbox::class.java)?.checked = false
            it.getGuiPart("草稿箱按钮", GermGuiCheckbox::class.java)?.checked = true
            it.getGuiPart("发件箱按钮", GermGuiCheckbox::class.java)?.checked = false
        }
        openGui(viewer)
        // 萌芽界面无法编辑多目标
        initMail(viewerData.getAllDraft())
    }

    private var openEdite: Boolean = false

    override fun initRightInfo(mail: IMail<*>, isOpen: Boolean, infoCanvas: GermGuiCanvas) {
        TODO("Not yet implemented")
    }

    private fun initRight(mail: IDraftBuilder, isOpen: Boolean, infoCanvas: GermGuiCanvas) {
        if (isOpen) {
            rightInfoCanvas.isEnable = true
        } else {
            rightInfoCanvas.isEnable = false
            return
        }
        rightInfoCanvas.getGuiPart("删除", GermGuiButton::class.java)?.apply {
            if (!isEnable) isEnable = true
            registerCallbackHandler({ _, _ ->
                if (openEdite) return@registerCallbackHandler
                if (mail.isAllowDeletion()) {
                    mail.deleteToSql {
                        if (it == 1) {
                            viewerData.delDraft(mail.unique)
                            rightInfoCanvas.isEnable = false
                            oldCheckbox?.checked = false
                            viewer.sendLang("玩家-删除草稿邮件-成功")
                        }
                    }
                }
            }, GermGuiButton.EventType.LEFT_CLICK)
        }
        rightInfoCanvas.getGuiPart("物品", GermGuiButton::class.java).apply {
            if (!isEnable) isEnable = true
            registerCallbackHandler({ _, _ ->
                if (openEdite) return@registerCallbackHandler
                //修改第一个，
                mail.getTargets().values.firstOrNull()?.let { data ->
                    if (data is MailDataNormal) {
                        openItems(data)
                    }
                }
            }, ClickType.LEFT_CLICK)
        }

        rightInfoCanvas.getGuiPart("保存", GermGuiButton::class.java).apply {
            if (!isEnable) isEnable = true
            interactCooldown = 2000
            registerCallbackHandler({ _, _ ->
                if (openEdite) return@registerCallbackHandler

                mail.updateToSql()
                viewer.sendLang("玩家-草稿邮件-保存", mail.title)
            }, ClickType.LEFT_CLICK)
        }

        // 发送
        rightInfoCanvas.getGuiPart("发送", GermGuiButton::class.java).apply {
            if (!isEnable) isEnable = true
            interactCooldown = 2000
            registerCallbackHandler({ _, _ ->
                if (openEdite) return@registerCallbackHandler
                this@EditeScreen.close()
                if (mail.isAllowSender()) {
                    player.closeInventory()
                    mail.senderMail()
                    viewerData.delDraft(mail.unique)
                }
            }, ClickType.LEFT_CLICK)
        }

        rightInfoCanvas.getGuiPart("信息画布", GermGuiCanvas::class.java).apply {
            getGuiPart("标题输入框", GermGuiInput::class.java).also { input ->
                if (input.isEnable) {
                    input.input = mail.title
                    input.registerCallbackHandler({ p, e ->
                        if (openEdite) return@registerCallbackHandler
                        if (e.input.isEmpty()) return@registerCallbackHandler
                        mail.title = e.input
                        // 更新图标，如果有的话
                        infoCanvas.getGuiPart("标题", GermGuiLabel::class.java).also { lable ->
                            if (lable.isEnable) {
                                lable.setText(e.input)
                            }
                        }
                    }, GermGuiInput.EventType.LOSE_FOCUS)
                }
            }
            getGuiPart("文本输入框", GermGuiInput::class.java).also { input ->
                if (input.isEnable) {
                    input.input = mail.context.joinToString(";")
                    input.registerCallbackHandler({ p, e ->
                        if (openEdite) return@registerCallbackHandler
                        if (e.input.isEmpty()) return@registerCallbackHandler
                        mail.context.clear()
                        mail.context.addAll(e.input.split(";"))
                    }, GermGuiInput.EventType.LOSE_FOCUS)
                }
            }
        }

        rightInfoCanvas.getGuiPart("物品画布", GermGuiCanvas::class.java)
            .getGuiPart("物品列表", GermGuiScroll::class.java).apply {
                mail.getTargets().values.firstOrNull()?.let {
                    it.getItemAppendix()?.forEachIndexed { index, item ->
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
            getGuiPart("金币文本输入框", GermGuiInput::class.java)?.let {
                if (it.isEnable) {
                    mail.getTargets().values.firstOrNull()?.let { data ->
                        if (data is MailDataNormal) {
                            it.input = data.money.toString()
                        }
                    }
                    it.registerCallbackHandler({ p, e ->
                        if (openEdite) return@registerCallbackHandler
                        if (e.input.isEmpty()) return@registerCallbackHandler
                        val amount = e.input.toIntOrNull() ?: 0
                        if (ProviderRegister.money?.hasTakeMoney(player, amount.toDouble()) == true) {
                            //修改第一个，
                            mail.getTargets().values.firstOrNull()?.let { data ->
                                if (data is MailDataNormal) {
                                    data.money = amount
                                    mail.updateToSql()
                                }
                            }
                        } else {
                            player.sendLang("邮件-编辑操作-金币不足")
                        }
                    }, GermGuiInput.EventType.LOSE_FOCUS)
                }
            }
            getGuiPart("点券文本解析", GermGuiInput::class.java)?.let {
                if (it.isEnable) {
                    mail.getTargets().values.firstOrNull()?.let { data ->
                        if (data is MailDataNormal) {
                            it.input = data.points.toString()
                        }
                    }
                    it.registerCallbackHandler({ p, e ->
                        if (openEdite) return@registerCallbackHandler
                        if (e.input.isEmpty()) return@registerCallbackHandler

                        val amount = e.input.toIntOrNull() ?: 0
                        if (ProviderRegister.points?.take(player, amount) == true) {
                            //修改第一个，
                            mail.getTargets().values.firstOrNull()?.let { data ->
                                if (data is MailDataNormal) {
                                    data.points = amount
                                    mail.updateToSql()
                                }
                            }
                        } else {
                            player.sendLang("邮件-编辑操作-点券不足")
                        }
                    }, GermGuiInput.EventType.LOSE_FOCUS)
                }
            }
        }
    }

    private fun openItems(data: MailDataNormal) {
        openEdite = true
        // 选择玩家
        val screen = object : GermGuiScreen(viewer.displayName+"-选择玩家", GermPluginLoader.itemsSection) {}
        screen.getGuiPart("总画布", GermGuiCanvas::class.java)?.let {
            it.getGuiPart("物品画布", GermGuiCanvas::class.java)?.let { slots ->
                var index = 1
                data.itemStacks.forEach { item ->
                    val slot = slots.getGuiPart("槽位-$index", GermGuiSlot::class.java)
                    slot.itemStack = item
                    index++
                }
            }
        }
        screen.setClosedHandler { _, _ ->
            openEdite = false
            screen.getGuiPart("总画布", GermGuiCanvas::class.java)?.let {
                it.getGuiPart("物品画布", GermGuiCanvas::class.java)?.let { slots ->
                    data.itemStacks.clear()
                    var index = 1
                    var slot = slots.getGuiPart("槽位-$index", GermGuiSlot::class.java)
                    while (slot != null) {
                        if (slot.itemStack.isNotAir()) {
                            data.itemStacks.add(slot.itemStack)
                        }
                        index++
                        slot = slots.getGuiPart("槽位-$index", GermGuiSlot::class.java)
                    }
                }
            }
        }
        screen.openChildGui(viewer)
    }

    private fun openPlayerSelect() {
        openEdite = true
        // 选择玩家
        val screen = object : GermGuiScreen(viewer.displayName+"-选择玩家", GermPluginLoader.playerSection) {}
        val scroll: GermGuiScroll

        screen.setClosedHandler { _, _ ->
            openEdite = false
        }

        screen.getGuiPart("总画布", GermGuiCanvas::class.java)?.let {
            it.getGuiPart("scroll", GermGuiScroll::class.java)!!.let { s ->
                scroll = s
                val temp = scroll.getGuiPart("player_list_canvas", GermGuiCanvas::class.java)!!.clone()
                scroll.removeGuiPart("player_list_canvas")
                Bukkit.getOnlinePlayers().forEach { pl ->
                    if (pl.name != player.name) {
                        val canvas = temp.clone()
                        canvas.indexName = pl.name
                        canvas.getGuiPart("玩家头颅", GermGuiItem::class.java)?.let { a ->
                            a.itemStack = Heads.getHead(pl.name)
                        }
                        canvas.getGuiPart("玩家名字", GermGuiLabel::class.java)?.let { a ->
                            a.texts = a.texts.map { text -> text.replacePlaceholder(pl) }
                        }
                        canvas.getGuiPart("选择玩家", GermGuiButton::class.java)?.let { a ->
                            a.registerCallbackHandler({ _, _ ->
                                // 萌芽默认创建符合类型
                                val new = NeonMailAPI.draftImpl.createNewInstance(
                                    player.uniqueId,
                                    MailDataNormal::class.java.simpleName
                                )
                                viewerData.addDraft(new)
                                // 创建完成
                                screen.close()
                                // 将选项设置
                                initMail(viewerData.getAllDraft(), new.unique)
                            }, GermGuiButton.EventType.LEFT_CLICK)
                        }
                        scroll.addGuiPart(canvas)
                    }
                }
            }
            it.getGuiPart("搜索确认", GermGuiButton::class.java)?.let { button ->
                button.registerCallbackHandler({ _, e ->
                    it.getGuiPart("搜索玩家", GermGuiInput::class.java)?.let { input ->
                        val text = input.input
                        if (text.isNotEmpty()) {
                            val list = scroll.getAllGuiParts(GermGuiCanvas::class.java).filter { ca ->
                                val gui = ca.getGuiPart("玩家名字", GermGuiLabel::class.java)
                                var out = false
                                if (gui != null) {
                                    for (a in gui.texts) {
                                        if (a == text || a.contains(text) || text.contains(a)) {
                                            out = true
                                            break
                                        }
                                    }
                                }
                                out
                            }
                            if (list.isNotEmpty()) {
                                scroll.clearGuiPart()
                                list.forEach(scroll::addGuiPart)
                            }
                        }
                    }

                }, GermGuiButton.EventType.LEFT_CLICK)
            }
        }
        screen.openChildGui(viewer)
    }
    override fun initMailCanvasCheckBox() {
        mailListCanvas.getGuiPart("创建", GermGuiButton::class.java)?.apply {
            if (!isEnable) isEnable = true
            registerCallbackHandler({ _, _ ->
                if (openEdite) return@registerCallbackHandler

                openPlayerSelect()
            }, GermGuiButton.EventType.LEFT_CLICK)
        }
        mailListCanvas.getGuiPart("搜索确认", GermGuiButton::class.java)?.apply {
            if (!isEnable) isEnable = true
            registerCallbackHandler({ _, _ ->
                if (openEdite) return@registerCallbackHandler

                mailListCanvas.getGuiPart("搜索框", GermGuiInput::class.java)?.input?.let {
                    if (it.isNotEmpty()) {
                        initMail(viewerData.getAllDraft().filter { a -> a.title.contains(it) })
                    }
                }
            }, GermGuiButton.EventType.LEFT_CLICK)
        }
    }

    private fun initMail(list: List<IDraftBuilder>, uuid: UUID? = null) {
        mailListCanvas.getGuiPart("邮件列表滚动", GermGuiScroll::class.java).apply {
            clearGuiPart()
            list.forEach {
                // 只接受萌芽创建的草稿箱
                if (it.getTargets().size == 1 && it.getTargets().values.firstOrNull() is MailDataNormal) {

                    val mail = mailInfoCanvas.clone()
                    if (!mail.isEnable) mail.isEnable = true

                    mail.getGuiPart("邮件复选框", GermGuiCheckbox::class.java).also { check ->
                        if (uuid != null && it.unique == uuid) {
                            if (oldCheckbox != null && oldCheckbox != check) {
                                check.checked = true
                                oldCheckbox?.checked = false
                                oldCheckbox = check
                            }
                            initRight(it, true, mail)
                        }
                        check.registerCallbackHandler({ _, _ ->
                            if (openEdite) return@registerCallbackHandler

                            val ac = !(check.checked as Boolean)
                            oldCheckbox?.isChecked = false
                            oldCheckbox = check
                            if (ac) check.isChecked = true

                            initRight(it, ac, mail)
                        }, GermGuiCheckbox.EventType.LEFT_CLICK)
                    }
                    mail.getGuiPart("标题", GermGuiLabel::class.java).also { label ->
                        if (label.isEnable) {
                            label.setText(it.title)
                        }
                    }
                    mail.getGuiPart("抄送目标", GermGuiLabel::class.java).also { label ->
                        if (label.isEnable) {
                            label.texts = label.texts.map { a ->
                                a.replace(
                                    "{target}",
                                    Bukkit.getOfflinePlayer(it.getTargets().keys.first()).name ?: "无法获取目标玩家"
                                )
                            }
                        }
                    }
                    mail.indexName = it.unique.toString()
                    addGuiPart(mail)
                }
            }
        }
    }

    override fun initMailCanvas(mailState: MailState) {}



}