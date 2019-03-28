package itravel.contract

import itravel.state.TicketClaimState
import net.corda.core.contracts.*
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.LedgerTransaction
import java.time.Instant


class LandAcquisitionContract : Contract {

    companion object {
        @JvmStatic
        val LAND_CONTRACT_ID = "itravel.contract.LandAcquisitionContract"
    }

    interface Commands : CommandData {
        class IssueLand : TypeOnlyCommandData(), Commands
        class IssueLandToPartyB : TypeOnlyCommandData(), Commands
    }

    @CordaSerializable
    enum class Status {
        PENDING_ISSUER_REVIEW,
        PENDING_ADVISORY_REVIEW,
        REQUESTED_FOR_CLAIM,
        REJECTED,
    }

    override fun verify(tx: LedgerTransaction) {
        println("in contract Ticket Claim Contract -------Start---" )
        val size=tx.commands.requireNoNulls().size
        println(size)

        val commands = tx.commands.requireNoNulls()

        var i:Int=0;

        for (command: CommandWithParties<CommandData> in commands){

            when (command.value) {
                is Commands.IssueLand -> {
                    println("------IssueLand command Issue land to PArty A------")
                    /*if (tx.outputs.size <1){
                        throw IllegalArgumentException("Failed requirement: during issuance of the ticket, the ticket "+
                                "output should be include in the transaction. " +
                                "Number of output states included was " + tx.outputs.size)
                    }*/
                }
                is Commands.IssueLandToPartyB -> {
                    println("------IssueLandToPartyB command Issue land to Party B------")
                    /*if (tx.outputs.size <1){
                        throw IllegalArgumentException("Failed requirement: during issuance of the ticket, the ticket "+
                                "output should be include in the transaction. " +
                                "Number of output states included was " + tx.outputs.size)
                    }*/
                }
            }
            i++
        }
    }
}