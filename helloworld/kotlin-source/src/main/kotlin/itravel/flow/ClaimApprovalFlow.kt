package itravel.flow

import co.paralleluniverse.fibers.Suspendable
import itravel.contract.TicketClaimContract
import itravel.state.TicketClaimState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.seconds

class ClaimApprovalFlow {

    @InitiatingFlow
    @StartableByRPC
    class ApproveByAirline(val reference: StateRef) : FlowLogic<SignedTransaction>() {
        companion object {
            object GENERATING_APPROVAL_TRANSACTION : ProgressTracker.Step("Generating LOC transaction.")
            object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction.")
            object FINALIZING : ProgressTracker.Step("Recording and distributing transaction.")

            fun tracker() = ProgressTracker(
                    GENERATING_APPROVAL_TRANSACTION,
                    SIGNING_TRANSACTION,
                    FINALIZING
            )
        }

        override val progressTracker = tracker()

        @Suspendable
        override fun call(): SignedTransaction {
            val applicationTxState = serviceHub.loadState(reference)
            val ticketClaim = applicationTxState.data as TicketClaimState
            println("In Call method---Airline---1--> Reference")
            println(reference)

            // Step 1. Generate transaction
            progressTracker.currentStep = GENERATING_APPROVAL_TRANSACTION

            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            println("In Call method-----Airline-2--> Status")
            println(ticketClaim.status)

            val builder = TransactionBuilder(notary)
            val appStateAndRef = StateAndRef(state = applicationTxState, ref = reference)
            builder.addInputState(appStateAndRef)
            builder.addOutputState(ticketClaim.copy(status = "APPROVED_BY_AIRLINE"), TicketClaimContract.CLAIM_CONTRACT_ID)

            builder.addCommand(TicketClaimContract.Commands.ApproveByAirline(), ticketClaim.airline.owningKey)
            println("In Call method----Airline--3-->")
            // Step 2. Add timestamp
            progressTracker.currentStep = SIGNING_TRANSACTION
            val currentTime = serviceHub.clock.instant()
            builder.setTimeWindow(currentTime, 30.seconds)
            println("In Call method----Airline--4-->")
            // Step 3. Verify transaction
            builder.verify(serviceHub)
            println("In Call method---Airline---5-->")
            // Step 4. Sign transaction
            progressTracker.currentStep = FINALIZING
            val stx = serviceHub.signInitialTransaction(builder)
            println("In Call method----Airline--6-->")
            // Step 5. Assuming no exceptions, we can now finalise the transaction.
            return subFlow(FinalityFlow(stx))
        }
    }


    @InitiatedBy(ApproveByAirline::class)
    class AcceptClaimbyAirline(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
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

    @InitiatingFlow
    @StartableByRPC
    class ApproveByInsurer(val reference: StateRef) : FlowLogic<SignedTransaction>() {
        companion object {
            object GENERATING_APPROVAL_TRANSACTION : ProgressTracker.Step("Generating LOC transaction.")
            object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction.")
            object FINALIZING : ProgressTracker.Step("Recording and distributing transaction.")

            fun tracker() = ProgressTracker(
                    GENERATING_APPROVAL_TRANSACTION,
                    SIGNING_TRANSACTION,
                    FINALIZING
            )
        }

        override val progressTracker = tracker()

        @Suspendable
        override fun call(): SignedTransaction {
            val applicationTxState = serviceHub.loadState(reference)
            val ticketClaim = applicationTxState.data as TicketClaimState
            print("In Call method----Insurer--1-->")
            // Step 1. Generate transaction
            progressTracker.currentStep = GENERATING_APPROVAL_TRANSACTION

            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            print("In Call method----Insurer--2-->")
            val builder = TransactionBuilder(notary)
            val appStateAndRef = StateAndRef(state = applicationTxState, ref = reference)
            builder.addInputState(appStateAndRef)
            builder.addOutputState(ticketClaim.copy(status = "APPROVED_BY_INSURER"), TicketClaimContract.CLAIM_CONTRACT_ID)

            builder.addCommand(TicketClaimContract.Commands.ApproveByInsurer(), ticketClaim.flightInsurer.owningKey)
            print("In Call method---Insurer---3-->")
            // Step 2. Add timestamp
            progressTracker.currentStep = SIGNING_TRANSACTION
            val currentTime = serviceHub.clock.instant()
            builder.setTimeWindow(currentTime, 30.seconds)
            print("In Call method---Insurer---4-->")
            // Step 3. Verify transaction
            builder.verify(serviceHub)
            print("In Call method---Insurer---5-->")
            // Step 4. Sign transaction
            progressTracker.currentStep = FINALIZING
            val stx = serviceHub.signInitialTransaction(builder)
            print("In Call method---Insurer---6-->")
            // Step 5. Assuming no exceptions, we can now finalise the transaction.
            return subFlow(FinalityFlow(stx))
        }
    }


    @InitiatedBy(ApproveByInsurer::class)
    class AcceptClaimByInsurer(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
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