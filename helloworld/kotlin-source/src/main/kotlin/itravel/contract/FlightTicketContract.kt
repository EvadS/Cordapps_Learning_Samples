package itravel.contract

import itravel.state.FlightTicketState
import itravel.state.TicketClaimState
import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant
import java.time.ZoneOffset

/**
 * An invoice is a document that describes a trade between a buyer and a buyer. It is issued on a particular date,
 * it lists goods being sold by the buyer, the cost of each good and the total amount owed by the buyer and when
 * the buyer expects to be paid by.
 *
 * In the trade finance world, invoices are used to create other contracts (for example AccountsReceivable), newly
 * created invoices start off with a status of "unassigned", once they're used to create other contracts the status
 * is changed to "assigned". This ensures that an invoice is used only once when creating a financial product like
 * AccountsReceivable.
 *
 */

class FlightTicketContract : Contract {

    companion object {
        @JvmStatic
        val TICKET_CONTRACT_ID = "itravel.contract.FlightTicketContract"
    }

    interface Commands : CommandData {
        class Issue : TypeOnlyCommandData(), Commands
        class RequestForClaim : TypeOnlyCommandData(), Commands
    }

    override fun verify(tx: LedgerTransaction) {

        println("in contract FlightTicket Contract -------Start---" )
        val size=tx.commands.requireNoNulls().size
        println(size)

        val commands = tx.commands.requireNoNulls();

        val time = Instant.now()
        var i:Int=0;

        for (command: CommandWithParties<CommandData> in commands){
            println("------ Contract Flight Ticket Command Iterate "+i+"------")
            println(command.value.toString());

            when (command.value) {
                is Commands.Issue -> {
                    println("------Flight Ticket command issue found------")

                    var osize:Int = tx.outputsOfType<FlightTicketState>().size

                    println("------Flight Ticket Output Size: ------"+ osize)

                    val issueOutput: FlightTicketState = tx.outputsOfType<FlightTicketState>()[osize-1];

                    println("------Output Airline: ------"+issueOutput)
                    println(issueOutput.airline)


                    println("------Output Amount: ------")
                    println(issueOutput.props.amount)

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
                    }
                }
                is Commands.RequestForClaim -> {
                    println("------Flight Ticket command Request for Claim found------")
                    var osize:Int = tx.outputsOfType<TicketClaimState>().size

                     println("------Flight Ticket Claim Output Size: ------"+ osize)

                     val issueOutput: TicketClaimState = tx.outputsOfType<TicketClaimState>()[osize-1];

                     println("------Output Airline: -1111----in Claim->>>>>>>>"+issueOutput)
                     println(issueOutput.airline)

                     println("------Output Amount: ---Claim---")
                     println(issueOutput.props.claimAmount)
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
            }
            i++;
        }


    }
}