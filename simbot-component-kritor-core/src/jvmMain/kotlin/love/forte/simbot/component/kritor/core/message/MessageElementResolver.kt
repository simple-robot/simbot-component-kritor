@file:JvmName("MessageElementResolver")

package love.forte.simbot.component.kritor.core.message

import io.kritor.event.ElementType
import io.kritor.event.ImageType
import io.kritor.event.MusicPlatform
import io.kritor.message.*
import love.forte.simbot.common.id.LongID.Companion.ID
import love.forte.simbot.common.id.NumericalID
import love.forte.simbot.common.id.StringID.Companion.ID
import love.forte.simbot.common.id.UIntID.Companion.ID
import love.forte.simbot.common.id.literal
import love.forte.simbot.component.kritor.core.message.KritorBubbleFace.Companion.toKritorBubbleFace
import love.forte.simbot.component.kritor.core.message.KritorFace.Companion.toKritorFace
import love.forte.simbot.component.kritor.core.message.KritorPoke.Companion.toKritorPoke
import love.forte.simbot.component.kritor.core.message.KritorReply.Companion.toKritorReply
import love.forte.simbot.component.kritor.core.message.internal.unknownKritorMessageElement
import love.forte.simbot.message.*
import love.forte.simbot.message.Message
import io.kritor.event.Contact as EventContact
import io.kritor.event.Element as EventElement
import io.kritor.event.ElementType as EventElementType
import io.kritor.event.ImageType as EventImageType
import io.kritor.event.MusicPlatform as EventMusicPlatform
import io.kritor.event.Scene as EventScene
import io.kritor.message.Contact as MsgContact
import io.kritor.message.ElementType as MsgElementType
import io.kritor.message.ImageType as MsgImageType
import io.kritor.message.MusicPlatform as MsgMusicPlatform
import io.kritor.message.Scene as MsgScene


internal inline fun Message.Element.resolveToKritorElements(block: (Message.Element, Element) -> Unit) {
    when (val e = this) {
        is KritorSendElementTransformer -> e.toElement()

        is PlainText -> block(e, element {
            type = MsgElementType.TEXT
            text = textElement { text = e.text }
        })

        is Face -> block(e, element {
            type = MsgElementType.FACE
            face = faceElement {
                id = (e.id as? NumericalID)?.toInt() ?: e.id.literal.toInt()
                // this.isBig
            }
        })

        is At -> block(e, element {
            type = MsgElementType.AT
            at = atElement {
                // uint64 uin = 1; // qq号，全体成员则uin为0
                // string uid = 2; // uid二选一，全体成员这里请写all
                val id = e.target
                if (id is NumericalID) {
                    uin = id.toLong()
                } else {
                    uid = id.literal
                }
            }
        })

        is AtAll -> block(e, element {
            type = MsgElementType.AT
            at = atElement {
                uin = 0
            }
        })

        is Image -> when (e) {
            is OfflineImage -> {
                // 一个本地文件，用于发送
                when (e) {
                    is OfflineFileImage -> {}
                    is OfflinePathImage -> {}
                    is OfflineURIImage -> {
                        val uri = e.uri
                        if (uri.scheme == "file") {
                            // 如果协议是 file: 那还是一个本地文件
                            TODO("uri(scheme=file)")

                        } else {
                            block(e, element {
                                type = MsgElementType.IMAGE
                                imageElement {
                                    this.url = uri.toASCIIString()
                                }
                            })
                        }
                    }

                    is OfflineResourceImage -> {
                        TODO()
                    }

                    else -> {
                        block(e, element {
                            type = MsgElementType.IMAGE
                            imageElement {
                                this.type
                            }
                        })

                    }
                }

            }

            is RemoteImage -> {
                // 一个远端文件 ..?

                TODO("RemoteImage")
            }

            else -> error("Unknown or unsupported image type") // TODO
        }
        // 其他？


    }
    // TODO  Message.resolveToKritorElements
}

internal inline fun Message.resolveToKritorElements(block: (Message.Element, Element) -> Unit) {
    if (this is Message.Element) {
        resolveToKritorElements(block)
    }

    (this as Messages).forEach { it.resolveToKritorElements(block) }
}

internal fun Message.resolveToKritorElementList(): List<Element> {
    return buildList {
        resolveToKritorElements { _, element -> add(element) }
    }
}

internal inline fun Message.resolveToSendMessageRequest(
    pre: SendMessageRequestKt.Dsl.() -> Unit = {},
    each: (Message.Element, Element) -> Element? = { _, element -> element },
    post: SendMessageRequestKt.Dsl.() -> Unit = {},
): SendMessageRequest {
    return sendMessageRequest {
        pre()
        resolveToKritorElements { me, e ->
            each(me, e)?.also { elements.add(it) }
        }
        post()
    }
}


internal fun EventElement.toMessageElement(): Element {
    val ee = this
    val eeType = type

    return element {
        this.type = eeType.resolve()
        when (eeType) {
            null -> throw IllegalArgumentException("Unknown event element type: null")
            EventElementType.TEXT -> {
                text = textElement {
                    text = ee.text.text
                }
            }

            EventElementType.AT -> {
                at = atElement {
                    if (ee.at.hasUin()) {
                        uin = ee.at.uin
                    } else {
                        uid = ee.at.uid
                    }
                }
            }

            EventElementType.FACE -> {
                face = faceElement {
                    this.id = ee.face.id
                    this.result = ee.face.result
                    this.isBig = ee.face.isBig
                }
            }

            EventElementType.BUBBLE_FACE -> {
                bubbleFace = bubbleFaceElement {
                    this.id = ee.bubbleFace.id
                    this.count = ee.bubbleFace.count
                }
            }

            EventElementType.REPLY -> {
                reply = replyElement {
                    this.messageId = ee.reply.messageId
                }
            }

            EventElementType.IMAGE -> {
                image = imageElement {
                    this.subType = ee.image.subType
                    this.file = ee.image.file
                    this.type = ee.image.type.resolve()
                    this.url = ee.image.url
                }
            }

            EventElementType.VOICE -> {
                voice = voiceElement {
                    this.file = ee.voice.file
                    this.url = ee.voice.url
                    this.magic = ee.voice.magic
                }
            }

            EventElementType.VIDEO -> {
                video = videoElement {
                    this.file = ee.video.file
                    this.url = ee.video.url
                }
            }

            EventElementType.BASKETBALL -> {
                basketball = basketballElement {
                    this.id = ee.basketball.id
                }
            }

            EventElementType.DICE -> {
                dice = diceElement {
                    this.id = ee.dice.id
                }
            }

            EventElementType.RPS -> {
                rps = rpsElement {
                    this.id = ee.rps.id
                }
            }

            EventElementType.POKE -> {
                poke = pokeElement {
                    this.id = ee.poke.id
                    this.type = ee.poke.type
                    this.strength = ee.poke.strength
                }
            }

            EventElementType.MUSIC -> {
                music = musicElement {
                    this.platform = ee.music.platform.resolve()
                    if (ee.music.hasId()) {
                        this.id = ee.music.id
                    } else if (ee.music.hasCustom()) {
                        val eec = ee.music.custom
                        this.custom = customMusicData {
                            this.url = eec.url
                            this.audio = eec.audio
                            this.title = eec.title
                            this.author = eec.author
                            this.pic = eec.pic
                        }
                    }
                }
            }

            EventElementType.WEATHER -> {
                weather = weatherElement {
                    this.city = ee.weather.city
                    this.code = ee.weather.code
                }
            }

            EventElementType.LOCATION -> {
                location = locationElement {
                    this.lat = ee.location.lat
                    this.lon = ee.location.lon
                    this.title = ee.location.title
                    this.address = ee.location.address
                }
            }

            EventElementType.SHARE -> {
                share = shareElement {
                    this.url = ee.share.url
                    this.title = ee.share.title
                    this.content = ee.share.content
                    this.image = ee.share.image
                }
            }

            EventElementType.GIFT -> {
                gift = giftElement {
                    this.qq = ee.gift.qq
                    this.id = ee.gift.id
                }
            }

            EventElementType.MARKET_FACE -> {
                marketFace = marketFaceElement {
                    this.id = ee.marketFace.id
                }
            }

            EventElementType.FORWARD -> {
                forward = forwardElement {
                    this.id = ee.forward.id
                    this.uniseq = ee.forward.uniseq
                    this.summary = ee.forward.summary
                    this.description = ee.forward.description
                }
            }

            EventElementType.CONTACT -> {
                contact = contactElement {
                    this.peer = ee.contact.peer
                    this.scene = ee.contact.scene.resolve()
                }
            }

            EventElementType.JSON -> {
                json = jsonElement {
                    this.json = ee.json.json
                }
            }

            EventElementType.XML -> {
                xml = xmlElement {
                    this.xml = ee.xml.xml
                }
            }

            EventElementType.FILE -> {
                file = fileElement {
                    this.name = ee.file.name
                    this.size = ee.file.size
                    this.expireTime = ee.file.expireTime
                    this.id = ee.file.id
                    this.url = ee.file.url
                    this.biz = ee.file.biz
                    this.subId = ee.file.subId
                }
            }

            EventElementType.MARKDOWN -> {
                markdown = markdownElement {
                    this.markdown = ee.markdown.markdown
                }
            }

            EventElementType.BUTTON -> {
                button = buttonElement {
                    if (ee.button.rowsCount > 0) {
                        for (eRow in ee.button.rowsList) {
                            rows.add(row {
                                if (eRow.buttonsCount > 0) {
                                    for (eButton in eRow.buttonsList) {
                                        buttons.add(button {
                                            this.id = eButton.id
                                            if (eButton.hasRenderData()) {
                                                this.renderData = buttonRender {
                                                    this.label = eButton.renderData.label
                                                    this.visitedLabel = eButton.renderData.visitedLabel
                                                    this.style = eButton.renderData.style
                                                }
                                            }
                                            if (eButton.hasAction()) {
                                                this.action = buttonAction {
                                                    this.type = eButton.action.type
                                                    this.permission = buttonActionPermission {
                                                        this.type = eButton.action.permission.type
                                                        this.roleIds.addAll(eButton.action.permission.roleIdsList)
                                                        this.userIds.addAll(eButton.action.permission.userIdsList)
                                                    }
                                                    this.unsupportedTips = eButton.action.unsupportedTips
                                                    this.data = eButton.action.data
                                                    this.reply = eButton.action.reply
                                                    this.enter = eButton.action.enter
                                                }
                                            }
                                        })
                                    }
                                }

                            })
                        }
                    }

                }
            }

            EventElementType.NODE -> {
                node = nodeElement {
                    this.messageId = ee.node.messageId
                    this.scene = ee.node.scene.resolve()
                }
            }

            EventElementType.UNRECOGNIZED -> {
                // do nothing.
            }
        }
    }
}

internal fun EventElementType.resolve(): MsgElementType {
    return when (this) {
        EventElementType.TEXT -> MsgElementType.TEXT
        EventElementType.AT -> MsgElementType.AT
        EventElementType.FACE -> MsgElementType.FACE
        EventElementType.BUBBLE_FACE -> MsgElementType.BUBBLE_FACE
        EventElementType.REPLY -> MsgElementType.REPLY
        EventElementType.IMAGE -> MsgElementType.IMAGE
        EventElementType.VOICE -> MsgElementType.VOICE
        EventElementType.VIDEO -> MsgElementType.VIDEO
        EventElementType.BASKETBALL -> MsgElementType.BASKETBALL
        EventElementType.DICE -> MsgElementType.DICE
        EventElementType.RPS -> MsgElementType.RPS
        EventElementType.POKE -> MsgElementType.POKE
        EventElementType.MUSIC -> MsgElementType.MUSIC
        EventElementType.WEATHER -> MsgElementType.WEATHER
        EventElementType.LOCATION -> MsgElementType.LOCATION
        EventElementType.SHARE -> MsgElementType.SHARE
        EventElementType.GIFT -> MsgElementType.GIFT
        EventElementType.MARKET_FACE -> MsgElementType.MARKET_FACE
        EventElementType.FORWARD -> MsgElementType.FORWARD
        EventElementType.CONTACT -> MsgElementType.CONTACT
        EventElementType.JSON -> MsgElementType.JSON
        EventElementType.XML -> MsgElementType.XML
        EventElementType.FILE -> MsgElementType.FILE
        EventElementType.MARKDOWN -> MsgElementType.MARKDOWN
        EventElementType.BUTTON -> MsgElementType.BUTTON
        EventElementType.NODE -> MsgElementType.NODE
        EventElementType.UNRECOGNIZED -> MsgElementType.UNRECOGNIZED
    }
}

internal fun EventImageType.resolve(): MsgImageType = when (this) {
    ImageType.COMMON -> MsgImageType.COMMON
    ImageType.ORIGIN -> MsgImageType.ORIGIN
    ImageType.FLASH -> MsgImageType.FLASH
    ImageType.UNRECOGNIZED -> MsgImageType.UNRECOGNIZED
}

internal fun EventMusicPlatform.resolve(): MsgMusicPlatform = when (this) {
    MusicPlatform.QQ -> MsgMusicPlatform.QQ
    MusicPlatform.NetEase -> MsgMusicPlatform.NetEase
    MusicPlatform.Custom -> MsgMusicPlatform.Custom
    MusicPlatform.UNRECOGNIZED -> MsgMusicPlatform.UNRECOGNIZED
}

internal fun EventScene.resolve(): MsgScene = when (this) {
    EventScene.GROUP -> MsgScene.GROUP
    EventScene.FRIEND -> MsgScene.FRIEND
    EventScene.GUILD -> MsgScene.GUILD
    EventScene.STRANGER_FROM_GROUP -> MsgScene.STRANGER_FROM_GROUP
    EventScene.NEARBY -> MsgScene.NEARBY
    EventScene.STRANGER -> MsgScene.STRANGER
    EventScene.UNRECOGNIZED -> MsgScene.UNRECOGNIZED
}

internal fun EventContact.resolve(): MsgContact {
    val ec = this
    return contact {
        this.peer = ec.peer
        this.subPeer = ec.subPeer
        this.scene = ec.scene.resolve()
    }
}

/**
 * 将 [EventElement] 集解析为 [Messages]
 */
public fun Iterable<EventElement>.toMessages(): Messages {
    return map { it.toMessage() }.toMessages()
}

/**
 * 将 [EventElement] 解析为 [Message]
 */
public fun EventElement.toMessage(): Message.Element {
    val ee = this
    val eeType = ee.type

    return when (eeType) {
        null -> unknownKritorMessageElement(ee)
        ElementType.TEXT -> ee.text.text.toText()
        ElementType.AT -> {
            val at = ee.at
            if (at.uid.equals("all", ignoreCase = true)) {
                AtAll
            } else {
                if (at.hasUin())
                    At(at.uin.ID) // LongID
                else
                    At(at.uid.ID) // StringID
            }
        }

        ElementType.FACE -> {
            val face = ee.face
            face.toKritorFace()
        }

        ElementType.BUBBLE_FACE -> {
            val bubbleFace = ee.bubbleFace
            bubbleFace.toKritorBubbleFace()
        }

        ElementType.REPLY -> {
            val reply = ee.reply
            reply.toKritorReply()
        }
        // ElementType.IMAGE -> TODO()
        // ElementType.VOICE -> TODO()
        // ElementType.VIDEO -> TODO()
         ElementType.BASKETBALL -> KritorBasketball(ee.basketball.id.toUInt().ID)
         ElementType.DICE -> KritorDice(ee.dice.id.toUInt().ID)
         ElementType.RPS -> KritorRps(ee.poke.id.toUInt().ID)
         ElementType.POKE -> {
             val poke = ee.poke
             poke.toKritorPoke()
         }
        // ElementType.MUSIC -> TODO()
        // ElementType.WEATHER -> TODO()
        // ElementType.LOCATION -> TODO()
        // ElementType.SHARE -> TODO()
        // ElementType.GIFT -> TODO()
        // ElementType.MARKET_FACE -> TODO()
        // ElementType.FORWARD -> TODO()
        // ElementType.CONTACT -> TODO()
        ElementType.JSON -> KritorJson(ee.json.json)
        ElementType.XML -> KritorXml(ee.xml.xml)
        // ElementType.FILE -> TODO()
        // ElementType.MARKDOWN -> TODO()
        // ElementType.BUTTON -> TODO()
        // ElementType.NODE -> TODO()
        // ElementType.UNRECOGNIZED -> TODO()
        else -> unknownKritorMessageElement(ee)
    }
}
