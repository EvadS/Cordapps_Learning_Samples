package itravel.state

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.Party

@JsonIgnoreProperties(ignoreUnknown=true)
data class LandAcquisition(
        val regulatory: Party,
        val seller: Party,
        val buyer: Party,
        val status: String

) : LinearState {
    override val linearId: UniqueIdentifier get() = UniqueIdentifier()
    override val participants get() = listOf( regulatory, seller, buyer)
}

