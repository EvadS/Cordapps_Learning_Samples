package itravel.flow

import co.paralleluniverse.fibers.Suspendable
import itravel.contract.FlightTicketContract
import itravel.state.FlightTicketState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.time.Duration
import java.time.Instant

object FlightTicketFlow {

    @InitiatingFlow
    @StartableByRPC
    class CreateTicket(val airline: Party, val flightInsurer: Party, val submittedTicket: FlightTicketState) : FlowLogic<SignedTransaction>() {
        companion object {
            object CREATING_BUILDER : ProgressTracker.Step("Creating builder")
            object ISSUING_TICKET : ProgressTracker.Step("Creating and Signing Ticket")
            object ADDING_STATES : ProgressTracker.Step("Adding Ticket state")
            object SENDING_TICKET : ProgressTracker.Step("Sending Ticket to Airline and Flight Insurer")
            object VERIFYING_TX : ProgressTracker.Step("Verifying transaction send by issuer")
            object SIGNING_TX : ProgressTracker.Step("Signing transaction")
            object SENDING_AIRLINE_TX : ProgressTracker.Step("Sending to airline")
            object SENDING_INSURER_TX : ProgressTracker.Step("Sending to flightInsurer")

            fun tracker() = ProgressTracker(CREATING_BUILDER, ISSUING_TICKET, ADDING_STATES, SENDING_AIRLINE_TX, SENDING_INSURER_TX, VERIFYING_TX, SIGNING_TX, SENDING_TICKET)
        }

        override val progressTracker: ProgressTracker = tracker()

        @Suspendable
        override fun call(): SignedTransaction {
            // Step 1. Get a reference to the notary service on our network and our key pair.
            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            println("******************************* Flow Started *****************************");
            println("Notary Found");

            progressTracker.currentStep = CREATING_BUILDER
            // Step 2. Create a new TransactionBuilder object.
            val builder = TransactionBuilder(notary)
            builder.setTimeWindow(Instant.now(), Duration.ofSeconds(60))

            println("CREATING_BUILDER");

            // Step 3. Create FlightTicketContract and command
            progressTracker.currentStep = ISSUING_TICKET
            val flightTicket = submittedTicket
            val issueCommand
                    = Command(FlightTicketContract.Commands.Issue(), listOf(serviceHub.myInfo.legalIdentities.first().owningKey))

            println("ISSUING_TICKET");

            // Step 4. Add the FlightTicketContract as an output state, as well as a command to the transaction builder.
            progressTracker.currentStep = ADDING_STATES
            builder.addOutputState(flightTicket, FlightTicketContract.TICKET_CONTRACT_ID)
            builder.addCommand(issueCommand)

            println("ADDING_STATES");

            // Step 5. Verify and sign it with our KeyPair.
            progressTracker.currentStep = VERIFYING_TX
            try {
                builder.verify(serviceHub)
                println("VERIFYING_TX");
            }catch(e:Exception){
                println("Ticket Exception handled----------"+e)
            }

            progressTracker.currentStep = SIGNING_TX
            val ptx = serviceHub.signInitialTransaction(builder)
            println("SIGNING_TX");

            // Step 6. Get counter-party signature for airline
            progressTracker.currentStep = SENDING_AIRLINE_TX
            val flowSessionairline = initiateFlow(airline)
            val stx1 = subFlow(CollectSignaturesFlow(ptx, setOf(flowSessionairline)))

            println("SENDING_AIRLINE_TX");

            // Step 6. Get counter-party signature for flight Insurer
            progressTracker.currentStep = SENDING_INSURER_TX
            val flowSessionflightInsurer = initiateFlow(flightInsurer)
            val stx2 = subFlow(CollectSignaturesFlow(stx1, setOf(flowSessionflightInsurer)))

            println("SENDING_INSURER_TX");

            println("******************************* Flow Finished *****************************");

            // Step 6. Assuming no exceptions, we can now finalise the transaction.
            return subFlow(FinalityFlow(stx2))
        }
    }

    @InitiatedBy(CreateTicket::class)
    class AcceptTicket(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val flow = object : SignTransactionFlow(counterpartySession) {
                @Suspendable
                override fun checkTransaction(stx: SignedTransaction) {
                }
            }
            val stx = subFlow(flow)
            return waitForLedgerCommit(stx.id)
        }
    }
}