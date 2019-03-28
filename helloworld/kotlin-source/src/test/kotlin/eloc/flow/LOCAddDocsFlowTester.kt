/*
package eloc.flow

import eloc.contract.PackingList
import eloc.flow.documents.BillOfLadingFlow
import eloc.flow.documents.BillOfLadingTimeline
import eloc.flow.documents.PackingListFlow
import eloc.helpers.bolProperties
import eloc.helpers.plProperties
import eloc.state.BillOfLadingState
import eloc.state.PackingListState
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.node.internal.StartedNode
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetwork.MockNode
import net.corda.testing.setCordappPackages
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant

*/
/**
 * Created by msreich on 4/13/17.
 *//*


class LOCAddDocsFlowTester {
    private val net: MockNetwork = MockNetwork()
    private lateinit var issuerNode: StartedNode<MockNode>
    private lateinit var beneficiaryNode: StartedNode<MockNode>
    private lateinit var advisingBankNode: StartedNode<MockNode>
    private lateinit var buyerNode: StartedNode<MockNode>
    private lateinit var notary: Party

    @Before
    fun setup() {
        setCordappPackages("eloc.contract")
        val nodes = net.createSomeNodes(numPartyNodes = 4)
        advisingBankNode = nodes.partyNodes[0]
        issuerNode = nodes.partyNodes[1]
        beneficiaryNode = nodes.partyNodes[2]
        buyerNode = nodes.partyNodes[3]
        notary = nodes.notaryNode.info.legalIdentities.first()
        net.runNetwork()

        nodes.partyNodes.forEach {
            it.registerInitiatedFlow(BillOfLadingFlow.ReceiveBol::class.java)
            it.registerInitiatedFlow(PackingListFlow.ReceivePackingList::class.java)
        }
    }

    @Test
    fun `record bill of lading`() {

        val initialState = BillOfLadingState(beneficiaryNode.info.legalIdentities.first(), buyerNode.info.legalIdentities.first(), advisingBankNode.info.legalIdentities.first(), issuerNode.info.legalIdentities.first(), Instant.now(), bolProperties)

        // kick off flow
        val sellerFlow = BillOfLadingFlow.UploadAndSend(initialState)
        val future = beneficiaryNode.services.startFlow(sellerFlow).resultFuture
        net.runNetwork()
        future.getOrThrow()

        listOf(beneficiaryNode, advisingBankNode).forEach { node ->
            val bolStates = node.database.transaction {
                node.services.vaultService.queryBy<BillOfLadingState>().states
            }
            assert(bolStates.count() > 0)
        }
    }

    @Test
    fun `record packing list`() {

        val initialState = PackingListState(beneficiaryNode.info.legalIdentities.first(), buyerNode.info.legalIdentities.first(), advisingBankNode.info.legalIdentities.first(), issuerNode.info.legalIdentities.first(), PackingList.Status.SIGNED, plProperties)

        // kick off flow
        val sellerFlow = PackingListFlow.UploadAndSend(initialState)
        val future = beneficiaryNode.services.startFlow(sellerFlow).resultFuture
        net.runNetwork()
        future.getOrThrow()

        listOf(beneficiaryNode, advisingBankNode).forEach { node ->
            val plStates = node.database.transaction {
                node.services.vaultService.queryBy<PackingListState>().states
            }
            assert(plStates.count() > 0)
        }
    }

    @Test
    fun `timeline`() {

        val initialState = BillOfLadingState(beneficiaryNode.info.legalIdentities.first(), buyerNode.info.legalIdentities.first(), advisingBankNode.info.legalIdentities.first(), issuerNode.info.legalIdentities.first(), Instant.now(), bolProperties)

        // kick off flow
        val sellerFlow = BillOfLadingFlow.UploadAndSend(initialState)
        val future = beneficiaryNode.services.startFlow(sellerFlow).resultFuture
        net.runNetwork()
        future.getOrThrow()

        val ref = beneficiaryNode.database.transaction {
            beneficiaryNode.services.vaultService.queryBy<BillOfLadingState>().states.first().state.data.props.billOfLadingID
        }

        val timelineFlow = BillOfLadingTimeline(ref)
        val timelineFuture = beneficiaryNode.services.startFlow(timelineFlow).resultFuture
        net.runNetwork()
        val result = timelineFuture.getOrThrow()

        assert(result.count() > 0)
    }

    @After
    fun tearDown() {
        net.stopNodes()
    }

}*/
