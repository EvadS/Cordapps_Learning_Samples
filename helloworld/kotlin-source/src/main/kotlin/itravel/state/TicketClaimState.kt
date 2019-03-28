package itravel.state

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import net.corda.core.contracts.*
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDate
import java.util.*

@JsonIgnoreProperties(ignoreUnknown=true)
data class TicketClaimState(
        val passenger: Party,
        val airline: Party,
        val flightInsurer: Party,
        val status: String,
        val props: TicketClaimProperties

) : LinearState {
    override val linearId: UniqueIdentifier get() = UniqueIdentifier()
    override val participants get() = listOf( passenger, airline, flightInsurer)
}

// Travel Ticket Claim Statement
@CordaSerializable
data class TicketClaimProperties (
        val flightTicketNo: String,
        val passengerName: String,
        val claimDate: LocalDate,
        val ticketClaimRef: StateRef,
        val claimAmount: Amount<Currency>,
        val attachmentHash: SecureHash

)