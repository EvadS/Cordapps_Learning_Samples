package itravel.flow

import co.paralleluniverse.fibers.Suspendable
import itravel.contract.FlightTicketContract
import itravel.contract.TicketClaimContract
import itravel.state.FlightTicketState
import itravel.state.TicketClaimState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.seconds


object TicketClaimFlow {

    @InitiatingFlow
    @StartableByRPC
    class CreateTicketClaim(val submittedTicketClaim: TicketClaimState) : FlowLogic<SignedTransaction>() {
        companion object {
            object GENERATING_CLAIM_TRANSACTION : ProgressTracker.Step("Generating LOC transaction.")
            object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction.")
            object FINALIZING : ProgressTracker.Step("Recording and distributing transaction.")

            fun tracker() = ProgressTracker(
                    GENERATING_CLAIM_TRANSACTION,
                    SIGNING_TRANSACTION,
                    FINALIZING
            )
        }

        override val progressTracker = tracker()

        @Suspendable
        override fun call(): SignedTransaction {
            println("coming in CreateTicketClaim step 1--call---->")
            val flightTicketTxState = serviceHub.loadState(submittedTicketClaim.props.ticketClaimRef)

            //val ticketClaim = ticketClaimTxState.data as TicketClaimState
            //val flightTicketTxState = serviceHub.loadState(ticketClaim.props.ticketClaimRef)

            val flightTicket = flightTicketTxState.data as FlightTicketState
            println("coming in CreateTicketClaim step 2--call---->")
            // Step 1. Generate transaction
            progressTracker.currentStep = GENERATING_CLAIM_TRANSACTION

            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            println("coming in CreateTicketClaim step 3--call---->")
            val builder = TransactionBuilder(notary)
            try {
                val appStateAndRef = StateAndRef(state = flightTicketTxState, ref = submittedTicketClaim.props.ticketClaimRef)
                println("coming in CreateTicketClaim step 4--call---->")
                builder.addInputState(appStateAndRef)
                builder.addOutputState(flightTicket.copy(status = "REQUESTED_FOR_CLAIM"), FlightTicketContract.TICKET_CONTRACT_ID)
                builder.addOutputState(submittedTicketClaim, TicketClaimContract.CLAIM_CONTRACT_ID)
                builder.addCommand(FlightTicketContract.Commands.RequestForClaim(), serviceHub.myInfo.legalIdentities.first().owningKey)
                builder.addCommand(TicketClaimContract.Commands.IssueClaim(), serviceHub.myInfo.legalIdentities.first().owningKey)
                println("coming in CreateTicketClaim step 5--call---->")

                // Step 2. Add timestamp
                progressTracker.currentStep = SIGNING_TRANSACTION
                val currentTime = serviceHub.clock.instant()
                builder.setTimeWindow(currentTime, 60.seconds)
                println("coming in CreateTicketClaim step 6--call---->")
                // Step 3. Verify transaction
                builder.verify(serviceHub)
                println("coming in CreateTicketClaim step 7--call---->")
                // Step 4. Sign transaction
                progressTracker.currentStep = FINALIZING
            } catch (e: Exception) {
                println("Exception handled----------"+e)
       }
            val stx = serviceHub.signInitialTransaction(builder)
            println("coming in CreateTicketClaim step 8--call---->")
            // Step 5. Assuming no exceptions, we can now finalise the transaction.
            return subFlow(FinalityFlow(stx))
        }
    }

    @InitiatedBy(CreateTicketClaim::class)
    class AcceptClaim(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
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