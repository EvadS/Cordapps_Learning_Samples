package itravel.contract

import itravel.state.TicketClaimState
import net.corda.core.contracts.*
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.LedgerTransaction
import java.time.Instant


class TicketClaimContract : Contract {

    companion object {
        @JvmStatic
        val CLAIM_CONTRACT_ID = "itravel.contract.TicketClaimContract"
    }

    interface Commands : CommandData {
        class IssueClaim : TypeOnlyCommandData(), Commands
        class ApproveByAirline : TypeOnlyCommandData(), Commands
        class ApproveByInsurer : TypeOnlyCommandData(), Commands
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
            println("------ Contract Ticket Claim Command Iterate "+i+"------")
            println(command.value.toString())
            println("------Flight Ticket command Request for Claim found------")
            var osize:Int = tx.outputsOfType<TicketClaimState>().size

            println("------Flight Ticket Claim Contract Output Size: ------"+ osize)

            val issueOutput: TicketClaimState = tx.outputsOfType<TicketClaimState>()[osize-1];

            println("------Output Airline: -1111----in Claim Contract->>>>>>>>"+issueOutput)
            println(issueOutput.airline)

            println("------Output Amount: ---Claim---")
            println(issueOutput.props.claimAmount)

            when (command.value) {
                is Commands.IssueClaim -> {
                    println("------Ticket claim command Issue Claim found------")
                    if (tx.outputs.size <1){
                        throw IllegalArgumentException("Failed requirement: during issuance of the ticket, the ticket "+
                                "output should be include in the transaction. " +
                                "Number of output states included was " + tx.outputs.size)
                    }
                    if (issueOutput.airline == null){
                        requireThat {
                            "There is no airline specified" using (issueOutput.airline == null)
                        }
                    } else if(issueOutput.flightInsurer == null){
                        requireThat {
                            "There is no Insurer specified" using (issueOutput.flightInsurer == null)
                        }
                    } else if(issueOutput.props.claimAmount == null){
                        requireThat {
                            "There is no Claim Amount specified" using (issueOutput.props.claimAmount == null)
                        }
                    }
                }

                is Commands.ApproveByAirline -> {
                    println("------Ticket claim command Approve By Airline found------")
                    if (tx.outputs.size <1){
                        throw IllegalArgumentException("Failed requirement: during issuance of the ticket, the ticket "+
                                "output should be include in the transaction. " +
                                "Number of output states included was " + tx.outputs.size)
                    }
                    if (issueOutput.airline == null){
                        requireThat {
                            "There is no airline specified" using (issueOutput.airline == null)
                        }
                    } else if(issueOutput.props.claimAmount == null){
                        requireThat {
                            "There is no Claim Amount specified" using (issueOutput.props.claimAmount == null)
                        }
                    }
                }

                is Commands.ApproveByInsurer -> {
                    println("------Ticket claim command Approve By Insurer found------")
                    if (tx.outputs.size <1){
                        throw IllegalArgumentException("Failed requirement: during issuance of the ticket, the ticket "+
                                "output should be include in the transaction. " +
                                "Number of output states included was " + tx.outputs.size)
                    }

                    if(issueOutput.flightInsurer == null){
                        requireThat {
                            "There is no Insurer specified" using (issueOutput.flightInsurer == null)
                        }
                    } else if(issueOutput.props.claimAmount == null){
                        requireThat {
                            "There is no Claim Amount specified" using (issueOutput.props.claimAmount == null)
                        }
                    }
                }
            }
            i++
        }

    }
}