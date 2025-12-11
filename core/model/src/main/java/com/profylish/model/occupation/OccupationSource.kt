package com.profylish.model.occupation

/**
 * Indicates the origin of the occupation data.
 */
enum class OccupationSource {
    ONET,
    ILO,
    CUSTOM,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): OccupationSource {
            return when (value?.trim()?.uppercase()) {
                "ONET", "O*NET" -> ONET
                "ILO" -> ILO
                "CUSTOM" -> CUSTOM
                else -> UNKNOWN
            }
        }
    }
}