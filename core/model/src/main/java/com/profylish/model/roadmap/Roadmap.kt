package com.profylish.model.roadmap

import androidx.compose.runtime.Immutable

@Immutable
data class RoadmapNode(
    val id: String,
    val title: String,
    val status: NodeStatus,
    val type: NodeType = NodeType.LESSON,
    val stars: Int = 0 // Tamamlanan derslerdeki başarı (0-3)
)

enum class NodeStatus {
    LOCKED,     // Gri, tıklanamaz
    ACTIVE,     // Renkli, animasyonlu, bir sonraki ders
    COMPLETED   // Altın rengi, tamamlanmış
}

enum class NodeType {
    LESSON,     // Standart yuvarlak ders
    CHEST,      // Ödül sandığı
    MILESTONE   // Büyük bitiş sınavı (Kupa vs.)
}