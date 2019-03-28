package itravel.state

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.*
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDate
import java.util.*

@JsonIgnoreProperties(ignoreUnknown=true)
data class LandAtoBState(
        val regulatory: Party,
        val seller: Party,
        val buyer: Party,
        val status: String,
        val props: LandProperties

) : LinearState {
    override val linearId: UniqueIdentifier get() = UniqueIdentifier()
    override val participants get() = listOf( regulatory, seller, buyer)
}

// Land Statement
@CordaSerializable
data class LandProperties (
        val landRef: StateRef,
        val attachmentHash: SecureHash

)

