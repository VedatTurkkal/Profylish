package com.profylish.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkOccupation(
    @SerialName("Job_Title_Clean")
    val jobTitle: String,

    @SerialName("SOC_Code")
    val socCode: String? = null,

    @SerialName("ONET_Title_Group")
    val onetGroup: String? = null,

    @SerialName("Source")
    val source: String? = null
)