/*
package eloc.flow

import eloc.contract.LOCApplication
import eloc.contract.LocDataStructures
import eloc.flow.documents.BillOfLadingFlow
import eloc.flow.loc.AdvisoryPaymentFlow
import eloc.flow.loc.IssuerPaymentFlow
import eloc.flow.loc.SellerPaymentFlow
import eloc.state.*
import net.corda.core.contracts.Amount
import net.corda.core.contracts.StateRef
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.finance.DOLLARS
import net.corda.node.internal.StartedNode
import net.corda.testing.node.MockNetwork
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.Period
import java.util.*
import net.corda.testing.*
import java.time.Instant

class PaymentFlowTests {
    init {
        setCordappPackages("eloc.contract", "net.corda.finance.contracts.asset")
    }
    lateinit var net: MockNetwork
    private lateinit var applicantNode: StartedNode<MockNetwork.MockNode>
    private lateinit var issuerNode: StartedNode<MockNetwork.MockNode>
    private lateinit var beneficiaryNode: StartedNode<MockNetwork.MockNode>
    private lateinit var advisingBankNode: StartedNode<MockNetwork.MockNode>
    private lateinit var notary: Party

    @Before
    fun setup() {
        net = MockNetwork()
        val nodes = net.createSomeNodes(numPartyNodes = 4)
        applicantNode = nodes.partyNodes[0]
        issuerNode = nodes.partyNodes[1]
        beneficiaryNode = nodes.partyNodes[2]
        advisingBankNode = nodes.partyNodes[3]
        notary = nodes.notaryNode.info.legalIdentities.first()

        nodes.partyNodes.forEach {
            it.registerInitiatedFlow(AdvisoryPaymentFlow.ReceivePayment::class.java)
            it.registerInitiatedFlow(IssuerPaymentFlow.ReceivePayment::class.java)
            it.registerInitiatedFlow(SellerPaymentFlow.ReceivePayment::class.java)
        }

        net.runNetwork()
    }

    @Test
    fun `make payment to seller`() {

        val futureApp = applicantNode.services.startFlow(LOCApplicationFlow.Apply(locApplicationState())).resultFuture
        net.runNetwork()
        val resultApp = futureApp.getOrThrow()

        // Run approval flow to create LOCState from application
        val stateAndRef = resultApp.tx.outRef<LOCApplicationState>(0)
        val futureApproval = issuerNode.services.startFlow(LOCApprovalFlow.Approve(reference = stateAndRef.ref)).resultFuture
        net.runNetwork()
        futureApproval.getOrThrow()

        val futureBol = beneficiaryNode.services.startFlow(BillOfLadingFlow.UploadAndSend(billOfLadingState())).resultFuture
        net.runNetwork()
        futureBol.getOrThrow()

        val futureCash = advisingBankNode.services.startFlow(SelfIssueCashFlow(1000000000.DOLLARS)).resultFuture
        net.runNetwork()
        futureCash.getOrThrow()

        val future = advisingBankNode.services.startFlow(SellerPaymentFlow.MakePayment(locId = stateAndRef.state.data.props.letterOfCreditApplicationID)).resultFuture
        net.runNetwork()
        future.getOrThrow()

        val issuerState = issuerNode.database.transaction {
            issuerNode.services.vaultService.queryBy<LOCState>().states.first()
        }

        val advisingState = advisingBankNode.database.transaction {
            advisingBankNode.services.vaultService.queryBy<LOCState>().states.first()
        }

        Assert.assertTrue(issuerState.state.data.beneficiaryPaid)
        Assert.assertTrue(advisingState.state.data.beneficiaryPaid)
    }

    @Test
    fun `make payment to advisory`() {

        val futureApp = applicantNode.services.startFlow(LOCApplicationFlow.Apply(locApplicationState())).resultFuture
        net.runNetwork()
        val resultApp = futureApp.getOrThrow()

        // Run approval flow to create LOCState from application
        val stateAndRef = resultApp.tx.outRef<LOCApplicationState>(0)
        val futureApproval = issuerNode.services.startFlow(LOCApprovalFlow.Approve(reference = stateAndRef.ref)).resultFuture
        net.runNetwork()
        futureApproval.getOrThrow()

        val futureBol = beneficiaryNode.services.startFlow(BillOfLadingFlow.UploadAndSend(billOfLadingState())).resultFuture
        net.runNetwork()
        futureBol.getOrThrow()

        val futureCash = issuerNode.services.startFlow(SelfIssueCashFlow(1000000000.DOLLARS)).resultFuture
        net.runNetwork()
        futureCash.getOrThrow()

        val future = issuerNode.services.startFlow(AdvisoryPaymentFlow.MakePayment(locId = stateAndRef.state.data.props.letterOfCreditApplicationID)).resultFuture
        net.runNetwork()
        future.getOrThrow()

        val issuerState = issuerNode.database.transaction {
            issuerNode.services.vaultService.queryBy<LOCState>().states.first()
        }

        val advisingState = advisingBankNode.database.transaction {
            advisingBankNode.services.vaultService.queryBy<LOCState>().states.first()
        }

        Assert.assertTrue(issuerState.state.data.advisoryPaid)
        Assert.assertTrue(advisingState.state.data.advisoryPaid)
    }

    @Test
    fun `make payment to issuer`() {

        val futureApp = applicantNode.services.startFlow(LOCApplicationFlow.Apply(locApplicationState())).resultFuture
        net.runNetwork()
        val resultApp = futureApp.getOrThrow()

        // Run approval flow to create LOCState from application
        val stateAndRef = resultApp.tx.outRef<LOCApplicationState>(0)
        val futureApproval = issuerNode.services.startFlow(LOCApprovalFlow.Approve(reference = stateAndRef.ref)).resultFuture
        net.runNetwork()
        futureApproval.getOrThrow()

        val futureBol = beneficiaryNode.services.startFlow(BillOfLadingFlow.UploadAndSend(billOfLadingState())).resultFuture
        net.runNetwork()
        futureBol.getOrThrow()

        val futureCash = applicantNode.services.startFlow(SelfIssueCashFlow(1000000000.DOLLARS)).resultFuture
        net.runNetwork()
        futureCash.getOrThrow()

        val future = applicantNode.services.startFlow(IssuerPaymentFlow.MakePayment(locId = stateAndRef.state.data.props.letterOfCreditApplicationID)).resultFuture
        net.runNetwork()
        future.getOrThrow()

        val issuerState = issuerNode.database.transaction {
            issuerNode.services.vaultService.queryBy<LOCState>().states.first()
        }

        val advisingState = advisingBankNode.database.transaction {
            advisingBankNode.services.vaultService.queryBy<LOCState>().states.first()
        }

        Assert.assertTrue(issuerState.state.data.issuerPaid)
        Assert.assertTrue(advisingState.state.data.issuerPaid)
    }

    private fun locApplicationState() : LOCApplicationState {
        val properties = LOCApplicationProperties (
                letterOfCreditApplicationID = "LOC01",
                applicationDate = LocalDate.of(2016,5,15),
                typeCredit = LocDataStructures.CreditType.SIGHT,
                amount = 100000.DOLLARS,
                expiryDate = LocalDate.of(2017,12,14),
                portLoading = LocDataStructures.Port("SG","Singapore",null,null,null),
                portDischarge = LocDataStructures.Port("US","Oakland",null,null,null),
                goods = listOf(LocDataStructures.PricedGood(description="Tiger balm",
                        quantity = 10000,
                        grossWeight = null,
                        unitPrice = Amount(1, Currency.getInstance("USD")),
                        purchaseOrderRef = null
                )),
                placePresentation = LocDataStructures.Location("US","California","Oakland"),
                lastShipmentDate = LocalDate.of(2016,6,12), // TODO does it make sense to include shipment date?
                periodPresentation = Period.ofDays(31),
                beneficiary = beneficiaryNode.info.chooseIdentity(),
                issuer = issuerNode.info.chooseIdentity(),
                applicant = applicantNode.info.chooseIdentity(),
                advisingBank = advisingBankNode.info.chooseIdentity(),
                invoiceRef = StateRef(SecureHash.Companion.randomSHA256(),0)
        )

        return LOCApplicationState(properties.applicant, properties.issuer, LOCApplication.Status.PENDING_ISSUER_REVIEW, properties, null)
    }

    private fun billOfLadingState() : BillOfLadingState {
        val properties = BillOfLadingProperties (
                billOfLadingID = "LOC01",
                issueDate = LocalDate.of(2017,12,14),
                carrierOwner = beneficiaryNode.info.chooseIdentity(),
                nameOfVessel = "CordaShip",
                descriptionOfGoods = listOf(LocDataStructures.Good(description="Crab meet cans",quantity = 10000,grossWeight = null)),
                portOfLoading = LocDataStructures.Port(country = "Morokko",city = "Larache",address = null,state = null,name=null),
                portOfDischarge = LocDataStructures.Port(country = "Belgium",city = "Antwerpen",address = null,state = null,name=null),
                grossWeight = LocDataStructures.Weight(quantity =  2500.0, unit = LocDataStructures.WeightUnit.KG),
                dateOfShipment = LocalDate.of(2016,5,15),
                shipper = LocDataStructures.Company(name = "Some company", address = "Some other address", phone = "+11 12345678"),
                notify = LocDataStructures.Person(name = "Some guy", address = "Some address", phone = "+11 23456789"),
                consignee = LocDataStructures.Company(name = "Some company", address = "Some other address", phone = "+11 12345678"),
                placeOfReceipt = LocDataStructures.Location(country = "USA", state ="Iowa", city = "Des Moines")
        )

        return BillOfLadingState(beneficiaryNode.info.chooseIdentity(),applicantNode.info.chooseIdentity(),advisingBankNode.info.chooseIdentity(),issuerNode.info.chooseIdentity(), Instant.now(), properties)
    }

    @After
    fun tearDown() {
        net.stopNodes()
    }

}*/
