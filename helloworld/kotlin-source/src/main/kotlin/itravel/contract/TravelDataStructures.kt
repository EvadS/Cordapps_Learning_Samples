package itravel.contract

import net.corda.core.serialization.CordaSerializable
import java.time.LocalDate

object TravelDataStructures {

    @CordaSerializable
    data class Passport(
            val passportNo: String,
            val passportIssueDate: LocalDate,
            val passportExpiryDate: LocalDate
    )

    @CordaSerializable
    data class Passenger(
            val name: String,
            val count: Int
    )

    @CordaSerializable
    enum class CreditType {
        SIGHT,
        DEFERRED_PAYMENT,
        ACCEPTANCE,
        NEGOTIABLE_CREDIT,
        TRANSFERABLE,
        STANDBY,
        REVOLVING,
        RED_CLAUSE,
        GREEN_CLAUSE
    }
}