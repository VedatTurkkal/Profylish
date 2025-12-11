package com.profylish.domain.usecase.occupation

import com.profylish.domain.repository.OccupationRepository
import com.profylish.model.occupation.Occupation
import javax.inject.Inject

/**
 * Meslek arama işlemini yöneten UseCase.
 * ViewModel bu sınıfı çağırır.
 */
class SearchOccupationsUseCase @Inject constructor(
    private val occupationRepository: OccupationRepository
) {
    /**
     * Operator 'invoke' sayesinde bu sınıfı bir fonksiyon gibi çağırabiliriz:
     * searchOccupationsUseCase("doctor")
     */
    suspend operator fun invoke(query: String): List<Occupation> {
        return if (query.isBlank()) {
            // Eğer kullanıcı bir şey yazmadıysa, popüler/önerilen meslekleri göster
            occupationRepository.getPopularOccupations()
        } else {
            // Bir şeyler yazdıysa, veritabanında/API'da ara
            // Küçük harfe çevirip trim yaparak arama kalitesini artırabiliriz
            occupationRepository.searchOccupations(query.trim())
        }
    }
}