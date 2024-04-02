package love.forte.simbot.component.kritor.core.message

import io.kritor.message.Element
import io.kritor.message.ElementType
import io.kritor.message.element
import io.kritor.message.weatherElement
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * KritorWeather
 *
 * @author Roy
 */
@Serializable
@SerialName("kritor.m.weather")
public data class KritorWeather(val city: String, val code: String) :
    KritorMessageElement, KritorSendElementTransformer {
    override fun toElement(): Element = element {
        type = ElementType.WEATHER
        weather = weatherElement {
            city = this@KritorWeather.city
            code = this@KritorWeather.code
        }
    }
}
