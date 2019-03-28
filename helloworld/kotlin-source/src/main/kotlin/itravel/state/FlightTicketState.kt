package itravel.state

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import itravel.contract.TravelDataStructures
import net.corda.core.contracts.Amount
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.TransactionBuilder
import java.time.LocalDate
import java.util.*

@JsonIgnoreProperties(ignoreUnknown=true)
data class FlightTicketState(
        val passenger: Party,
        val airline: Party,
        val flightInsurer: Party,
        val status: String,
        val props: TravelProperties

) : LinearState {
    override val linearId: UniqueIdentifier get() = UniqueIdentifier()
    override val participants get() = listOf( passenger, airline, flightInsurer)
}

// Travel Statement
@CordaSerializable
data class TravelProperties (
        val ticketNo: String,
        val passport: TravelDataStructures.Passport,
        val passengerName: String,
        val passengerCount: Int,
        val amount: Amount<Currency>,
        val attachmentHash: SecureHash
)