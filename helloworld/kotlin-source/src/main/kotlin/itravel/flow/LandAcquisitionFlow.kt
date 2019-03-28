package itravel.flow

import co.paralleluniverse.fibers.Suspendable
import itravel.contract.FlightTicketContract
import itravel.contract.LandAcquisitionContract
import itravel.contract.TicketClaimContract
import itravel.state.FlightTicketState
import itravel.state.LandAcquisition
import itravel.state.LandAtoBState
import itravel.state.TicketClaimState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.seconds


object LandAcquisitionFlow {

    @InitiatingFlow
    @StartableByRPC
    class sellByRegulatory(val submittedTicketClaim: LandAcquisition) : FlowLogic<SignedTransaction>() {
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
            // Step 1. Generate transaction
            progressTracker.currentStep = GENERATING_CLAIM_TRANSACTION
            println("coming in CreateTicketClaim step 2--call---->")

            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            println("coming in step 3--call---->")
            val builder = TransactionBuilder(notary)
            try {
                println("coming in step 4--call---->")
                builder.addOutputState(submittedTicketClaim.copy(status = "FIRST_SELL"), LandAcquisitionContract.LAND_CONTRACT_ID)
                builder.addCommand(LandAcquisitionContract.Commands.IssueLand(), serviceHub.myInfo.legalIdentities.first().owningKey)
                println("coming in step 5--call---->")

                // Step 2. Add timestamp
                progressTracker.currentStep = SIGNING_TRANSACTION
                val currentTime = serviceHub.clock.instant()
                builder.setTimeWindow(currentTime, 60.seconds)
                println("coming in step 6--call---->")
                // Step 3. Verify transaction
                builder.verify(serviceHub)
                println("coming in step 7--call---->")
                // Step 4. Sign transaction
                progressTracker.currentStep = FINALIZING
            } catch (e: Exception) {
                println("Exception handled----------" + e)
            }
            val stx = serviceHub.signInitialTransaction(builder)
            println("coming in step 8--call---->")
            // Step 5. Assuming no exceptions, we can now finalise the transaction.
            return subFlow(FinalityFlow(stx))
        }
    }

    @InitiatedBy(sellByRegulatory::class)
    class AcceptLandByPartyA(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
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
        class sellByPartyA(val reference: StateRef, val seller: Party, val buyer: Party) : FlowLogic<SignedTransaction>() {
            companion object {
                object GENERATING_LAND_TRANSACTION : ProgressTracker.Step("Generating land transaction.")
                object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction.")
                object FINALIZING : ProgressTracker.Step("Recording and distributing transaction.")

                fun tracker() = ProgressTracker(
                        GENERATING_LAND_TRANSACTION,
                        SIGNING_TRANSACTION,
                        FINALIZING
                )
            }

            override val progressTracker = tracker()

            @Suspendable
            override fun call(): SignedTransaction {
                val applicationTxState = serviceHub.loadState(reference)
                val landState = applicationTxState.data as LandAcquisition
                println("In Call method------1--> Reference")
                println(reference)


                // Step 1. Generate transaction
                progressTracker.currentStep = GENERATING_LAND_TRANSACTION

                val notary = serviceHub.networkMapCache.notaryIdentities.first()
                println("In Call method------2--> Status")
                println(landState.status)

                val builder = TransactionBuilder(notary)
                val appStateAndRef = StateAndRef(state = applicationTxState, ref = reference)
                builder.addInputState(appStateAndRef)
                builder.addOutputState(landState.copy(status = "SECOND_SELL", seller = seller, buyer= buyer), LandAcquisitionContract.LAND_CONTRACT_ID)
                builder.addCommand(LandAcquisitionContract.Commands.IssueLandToPartyB(), serviceHub.myInfo.legalIdentities.first().owningKey)

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

        @InitiatedBy(sellByPartyA::class)
        class AcceptLandByPartyB(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
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