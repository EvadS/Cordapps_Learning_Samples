package net.corda.training.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.training.contract.IOUContract
import net.corda.training.state.IOUState
import java.util.*

/**
 *“
 */
@InitiatingFlow
@StartableByRPC
class IOUIssueFlow( val amount: Int, val currency: String,  val lender: Party)
   //(val state: IOUState)

    : FlowLogic<SignedTransaction>() {

    object GENERATING_TRANSACTION : ProgressTracker.Step("Waiting for seller trading info")
    object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction.")
    object FINALIZING : ProgressTracker.Step("Recording and distributing transaction.")

    override val progressTracker = ProgressTracker(GENERATING_TRANSACTION,SIGNING_TRANSACTION,FINALIZING)

    @Suspendable
    override fun call(): SignedTransaction {




        // Step 1. Get a reference to the notary service on our network and our key pair.
        // Note: ongoing work to support multiple notary identities is still in progress.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val me = serviceHub.myInfo.legalIdentities.first()
        val state = IOUState(Amount(amount.toLong() * 100, Currency.getInstance(currency)), lender, me)

        // Step 2. Create a new issue command.
        // Remember that a command is a CommandData object and a list of CompositeKeys
        val issueCommand = Command(IOUContract.Commands.Issue(), state.participants.map { it.owningKey })

        // Step 3. Create a new TransactionBuilder object.
        val builder = TransactionBuilder(notary = notary)

        // Step 4. Add the iou as an output state, as well as a command to the transaction builder.
        builder.addOutputState(state, IOUContract.IOU_CONTRACT_ID)
        builder.addCommand(issueCommand)

        // Step 5. Verify and sign it with our KeyPair.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        val sessions = (state.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        // Step 6. Collect the other party's signature using the SignTransactionFlow.
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        // Step 7. Assuming no exceptions, we can now finalise the transaction.
        return subFlow(FinalityFlow(stx))
    }
}

/**
 * This is the flow which signs IOU issuances.
 * The signing is handled by the [SignTransactionFlow].
 */
@InitiatedBy(IOUIssueFlow::class)
class IOUIssueFlowResponder(val flowSession: FlowSession): FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be an IOU transaction" using (output is IOUState)
            }
        }
        subFlow(signedTransactionFlow)
    }
}