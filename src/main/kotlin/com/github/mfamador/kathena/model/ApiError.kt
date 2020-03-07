package com.github.mfamador.kathena.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class ApiError @JsonCreator constructor(@JsonInclude(JsonInclude.Include.NON_NULL)
                                             @JsonProperty("personId") val personId: String?,
                                             @JsonProperty("message") val message: String) {

    constructor(message: String) : this(null, message)
}
