package net.cyclestreets.routing.domain

import com.fasterxml.jackson.annotation.JsonProperty
// Used for circular routes
class PoiDomainObject {
    @JsonProperty
    val poitypeId: String? = null
    @JsonProperty
    val name: String? = null
    @JsonProperty
    val website: String? = null
    @JsonProperty
    val longitude: Float = 0.0F
    @JsonProperty
    val latitude: Float = 0.0F
    @JsonProperty
    val sequenceId: Short = 0
}
