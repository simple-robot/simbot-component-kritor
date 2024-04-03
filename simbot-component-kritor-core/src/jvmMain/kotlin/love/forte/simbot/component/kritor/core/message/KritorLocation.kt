package love.forte.simbot.component.kritor.core.message

import io.kritor.message.Element
import io.kritor.message.ElementType
import io.kritor.message.element
import io.kritor.message.locationElement
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Kritor 的 Location
 *
 * @author Roy
 */
@Serializable
@SerialName("kritor.m.location")
public data class KritorLocation(val lat: Float, val lon: Float, val title: String, val address: String) :
    KritorMessageElement, KritorSendElementTransformer {

    public companion object {
        @JvmStatic
        @JvmName("valueOf")
        public fun io.kritor.event.LocationElement.toKritorLocation(): KritorLocation {
            return KritorLocation(lat, lon, title, address)
        }
    }

    override fun toElement(): Element = element {
        type = ElementType.LOCATION
        location = locationElement {
            this@KritorLocation.lat
            this@KritorLocation.lon
            this@KritorLocation.title
            this@KritorLocation.address
        }
    }
}
