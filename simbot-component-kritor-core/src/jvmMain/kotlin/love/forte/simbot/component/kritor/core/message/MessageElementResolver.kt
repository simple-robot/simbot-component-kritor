@file:JvmName("MessageElementResolver")

package love.forte.simbot.component.kritor.core.message

import io.kritor.event.ImageType
import io.kritor.event.MusicPlatform
import io.kritor.message.*
import love.forte.simbot.common.id.LongID.Companion.ID
import love.forte.simbot.common.id.NumericalID
import love.forte.simbot.common.id.StringID.Companion.ID
import love.forte.simbot.common.id.UIntID.Companion.ID
import love.forte.simbot.common.id.ULongID.Companion.ID
import love.forte.simbot.common.id.literal
import love.forte.simbot.component.kritor.core.message.KritorBubbleFace.Companion.toKritorBubbleFace
import love.forte.simbot.component.kritor.core.message.KritorContactElement.Companion.toKritorContact
import love.forte.simbot.component.kritor.core.message.KritorFace.Companion.toKritorFace
import love.forte.simbot.component.kritor.core.message.KritorForward.Companion.toKritorForward
import love.forte.simbot.component.kritor.core.message.KritorLocation.Companion.toKritorLocation
import love.forte.simbot.component.kritor.core.message.KritorMusic.Companion.toKritorMusic
import love.forte.simbot.component.kritor.core.message.KritorPoke.Companion.toKritorPoke
import love.forte.simbot.component.kritor.core.message.KritorReply.Companion.toKritorReply
import love.forte.simbot.component.kritor.core.message.KritorShare.Companion.toKritorShare
import love.forte.simbot.component.kritor.core.message.internal.toRemoteImage
import love.forte.simbot.component.kritor.core.message.internal.toRemoteVoice
import love.forte.simbot.component.kritor.core.message.internal.unknownKritorMessageElement
import love.forte.simbot.message.*
import love.forte.simbot.message.Message
import love.forte.simbot.resource.FileResource
import love.forte.simbot.resource.PathResource
import love.forte.simbot.resource.URIResource
import java.io.File
import java.net.URI
import java.nio.file.Path
import java.util.*
import kotlin.io.path.readBytes
import kotlin.io.path.toPath
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


internal suspend inline fun Message.Element.resolveToKritorElements(block: (Message.Element, Element) -> Unit) {
    when (val e = this) {
        is KritorSendElementTransformer -> e.toElement()

        is PlainText -> block(
            e,
            element {
                type = MsgElementType.TEXT
                text = textElement { text = e.text }
            }
        )

        is Face -> block(
            e,
            element {
                type = MsgElementType.FACE
                face = faceElement {
                    id = (e.id as? NumericalID)?.toInt() ?: e.id.literal.toInt()
                    // this.isBig
                }
            }
        )

        is At -> block(
            e,
            element {
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
            }
        )

        is AtAll -> block(
            e,
            element {
                type = MsgElementType.AT
                at = atElement {
                    uin = 0
                }
            }
        )

        is Image -> when (e) {
            is OfflineImage -> {
                // 一个本地文件，用于发送
                when (e) {
                    is OfflineFileImage, is OfflinePathImage -> {
                        val b64 = when (e) {
                            is OfflineFileImage -> e.file.b64()
                            is OfflinePathImage -> e.path.b64()
                            else -> error("Unknown image type") // 不可达
                        }

                        b64ImgElement(b64, e, block)
                    }

                    is OfflineURIImage -> {
                        val uri = e.uri
                        if (uri.isFileScheme) {
                            // 如果协议是 `file` 那还是一个本地文件,
                            // 直接读取bytes然后b64伺候
                            val b64 = uri.toPath().b64()
                            b64ImgElement(b64, e, block)
                        } else {
                            // 要么就当它是个URL图片，直接提供URL
                            urlImgElement(uri.toASCIIString(), e, block)
                        }
                    }

                    is OfflineResourceImage -> {
                        when (val resource = e.resource) {
                            is FileResource, is PathResource -> {
                                val b64 = when (resource) {
                                    is FileResource -> resource.file.b64()
                                    is PathResource -> resource.path.b64()
                                    else -> error("Unknown resource type") // 不可达
                                }

                                b64ImgElement(b64, e, block)
                            }

                            is URIResource -> {
                                val uri = resource.uri
                                // 文件，读b64，或连接地址
                                if (resource.uri.isFileScheme) {
                                    val b64 = uri.toPath().b64()
                                    b64ImgElement(b64, e, block)
                                } else {
                                    urlImgElement(uri.toASCIIString(), e, block)
                                }
                            }

                            // 其他: 尝试使用 `data()` 然后b64
                            else -> {
                                val b64 = Base64.getEncoder().encodeToString(resource.data())
                                b64ImgElement(b64, e, block)
                            }
                        }
                    }
                    // 其他: 尝试使用 `data()` 然后b64
                    else -> {
                        val b64 = Base64.getEncoder().encodeToString(e.data())
                        b64ImgElement(b64, e, block)
                    }
                }
            }

            is RemoteImage -> {
                // 一个远端文件
                when (e) {
                    // is KritorRemoteEventElementImage
                    // 已经被 KritorSendElementTransformer 处理

                    // 其他未知的其他远程图片
                    // 要么知道它的url，要么没法解析
                    is RemoteUrlAwareImage -> {
                        val url = e.url()
                        b64ImgElement(url, e, block = block)
                    }

                    else -> throw IllegalArgumentException("Unsupported RemoteImage type: $e")
                }
            }

            else -> error("Unknown or unsupported image type") // TODO
        }

        // 其他？


    }
// TODO  Message.resolveToKritorElements
}

internal suspend inline fun Message.resolveToKritorElements(block: (Message.Element, Element) -> Unit) {
    if (this is Message.Element) {
        resolveToKritorElements(block)
    }

    (this as Messages).forEach { it.resolveToKritorElements(block) }
}

internal suspend fun Message.resolveToKritorElementList(): List<Element> {
    return buildList {
        resolveToKritorElements { _, element -> add(element) }
    }
}

internal suspend inline fun Message.resolveToSendMessageRequest(
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

internal val URI.isFileScheme: Boolean
    get() = scheme == "file"

internal fun File.b64(): String =
    Base64.getEncoder().encodeToString(readBytes())

internal fun Path.b64(): String =
    Base64.getEncoder().encodeToString(readBytes())

internal fun ByteArray.b64(): String =
    Base64.getEncoder().encodeToString(this)

private inline fun b64ImgElement(
    b64: String,
    e: Message.Element,
    block: (Message.Element, Element) -> Unit,
    imageBlock: ImageElementKt.Dsl.() -> Unit = {}
) {
    block(
        e,
        element {
            type = MsgElementType.IMAGE
            image = imageElement {
                fileBase64 = b64
                imageBlock()
            }
        }
    )
}

private inline fun urlImgElement(
    url: String,
    e: Message.Element,
    block: (Message.Element, Element) -> Unit,
    imageBlock: ImageElementKt.Dsl.() -> Unit = {}
) {
    block(
        e,
        element {
            type = MsgElementType.IMAGE
            image = imageElement {
                this.url = url
                imageBlock()
            }
        }
    )
}

@Suppress("MaxLineLength")
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

            EventElementType.BUTTON
            -> {
                button = buttonElement {
                    if (ee.button.rowsCount > 0) {
                        for (eRow in ee.button.rowsList) {
                            rows.add(
                                row {
                                    if (eRow.buttonsCount > 0) {
                                        for (eButton in eRow.buttonsList) {
                                            buttons.add(
                                                button {
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
                                                }
                                            )
                                        }
                                    }

                                }
                            )
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
        EventElementType.TEXT -> ee.text.text.toText()
        EventElementType.AT -> {
            val at = ee.at
            if (at.uid.equals("all", ignoreCase = true)) {
                AtAll
            } else {
                if (at.hasUin()) {
                    At(at.uin.ID) // LongID
                } else {
                    At(at.uid.ID) // StringID
                }
            }
        }

        EventElementType.FACE -> {
            val face = ee.face
            face.toKritorFace()
        }

        EventElementType.BUBBLE_FACE -> {
            val bubbleFace = ee.bubbleFace
            bubbleFace.toKritorBubbleFace()
        }

        EventElementType.REPLY -> {
            val reply = ee.reply
            reply.toKritorReply()
        }

        EventElementType.IMAGE -> ee.image.toRemoteImage()
        EventElementType.VOICE -> ee.voice.toRemoteVoice()
        // EventElementType.VIDEO -> TODO()
        EventElementType.BASKETBALL -> KritorBasketball(ee.basketball.id.toUInt().ID)
        EventElementType.DICE -> KritorDice(ee.dice.id.toUInt().ID)
        EventElementType.RPS -> KritorRps(ee.poke.id.toUInt().ID)
        EventElementType.POKE -> ee.poke.toKritorPoke()
        EventElementType.MUSIC -> ee.music.toKritorMusic()
        EventElementType.WEATHER -> KritorWeather(ee.weather.city, ee.weather.code)
        EventElementType.LOCATION -> ee.location.toKritorLocation()
        EventElementType.SHARE -> ee.share.toKritorShare()
        EventElementType.GIFT -> KritorGift(ee.gift.qq.toULong().ID, ee.gift.id.toUInt().ID)
        EventElementType.MARKET_FACE -> KritorMarketFace(ee.marketFace.id.ID, ee.markdown.markdown)
        EventElementType.FORWARD -> ee.forward.toKritorForward()
        EventElementType.CONTACT -> ee.contact.toKritorContact()
        EventElementType.JSON -> KritorJson(ee.json.json)
        EventElementType.XML -> KritorXml(ee.xml.xml)
        // EventElementType.FILE -> TODO()
        // EventElementType.MARKDOWN -> TODO()
        // EventElementType.BUTTON -> TODO()
        // EventElementType.NODE -> TODO()
        // EventElementType.UNRECOGNIZED -> TODO()
        else -> unknownKritorMessageElement(ee)
    }
}
