package net.corda.training.api

import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import net.corda.core.contracts.Amount
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.internal.x500Name
import net.corda.core.messaging.*
import net.corda.core.node.NodeInfo
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.ColumnPredicate
import net.corda.core.node.services.vault.DEFAULT_PAGE_NUM
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import net.corda.finance.contracts.asset.Cash
import net.corda.finance.contracts.getCashBalances
import net.corda.nodeapi.internal.persistence.contextTransaction

import net.corda.training.QueryTransactionCountFlow
import net.corda.training.QueryTransactionCountFlowWithRange
import net.corda.training.flow.*
import net.corda.training.state.IOUState
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.hibernate.cfg.annotations.QueryBinder
import org.slf4j.Logger
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import java.time.Instant
import java.util.stream.Collectors


/**
 * This API is accessible from /api/iou. The endpoint paths specified below are relative to it.
 * We've defined a bunch of endpoints to deal with IOUs, cash and the various operations you can perform with them.
 */
@Path("iou")
class IOUApi(val rpcOps: CordaRPCOps) {
    private val me = rpcOps.nodeInfo().legalIdentities.first().name
    private val myLegalName = me.x500Name

    companion object {
        private val logger: Logger = loggerFor<IOUApi>()
    }

    fun X500Name.toDisplayString(): String = BCStyle.INSTANCE.toString(this)

    /** Helpers for filtering the network map cache. */
    private fun isNotary(nodeInfo: NodeInfo) = rpcOps.notaryIdentities().any { nodeInfo.isLegalIdentity(it) }

    private fun isMe(nodeInfo: NodeInfo) = nodeInfo.legalIdentities.first().name == me
    private fun isNetworkMap(nodeInfo: NodeInfo) = nodeInfo.legalIdentities.single().name.organisation == "Network Map Service"

    /**
     * Returns the node's name.
     */
    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoami() = mapOf("me" to myLegalName.toDisplayString())

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPeers(): Map<String, List<String>> {
        return mapOf("peers" to rpcOps.networkMapSnapshot()
                .filter { isNotary(it).not() && isMe(it).not() && isNetworkMap(it).not() }
                .map { it.legalIdentities.first().name.x500Name.toDisplayString() })
    }

    /**
     * Task 1
     * Displays all IOU states that exist in the node's vault.
     * TODO: Return a list of IOUStates on ledger
     * Hint - Use [rpcOps] to query the vault all unconsumed [IOUState]s
     */
    @GET
    @Path("ious")
    @Produces(MediaType.APPLICATION_JSON)
    fun getIOUs(): List<StateAndRef<ContractState>> {
        // Filter by state type: IOU.
        return rpcOps.vaultQueryBy<IOUState>().states
    }


    @GET
    @Path("transactions-by-period")
    @Produces(MediaType.APPLICATION_JSON)
    fun getTransatcionsByPeriod(@QueryParam(value = "startDate") startDate: Long,
                                @QueryParam(value = "endDate") endDate: Long): Int {


        var startDateInput = startDate
        var endDateInput = endDate

        if (endDate < startDate) {
            startDateInput = endDate
            endDateInput = startDate
        }


        val start = Instant.ofEpochMilli(startDateInput)
        val end = Instant.ofEpochMilli(endDateInput)

        val recordedBetweenExpression = QueryCriteria.TimeCondition(
                QueryCriteria.TimeInstantType.RECORDED,
                ColumnPredicate.Between(start, end))

        val criteria = QueryCriteria.VaultQueryCriteria(timeCondition = recordedBetweenExpression, status = Vault.StateStatus.ALL)
        val results = rpcOps.vaultQueryBy<ContractState>(criteria)



        return results.states.size
    }
/*
    private fun getTransactionCount ( date :Long ) : Int{
        val end = Instant.ofEpochMilli(date)

        val recordedBetweenExpression = QueryCriteria.TimeCondition(
                QueryCriteria.TimeInstantType.RECORDED,
                ColumnPredicate //.Between(start, end))

        val criteria = QueryCriteria.VaultQueryCriteria(timeCondition = recordedBetweenExpression,status = Vault.StateStatus.ALL)
        val results = rpcOps.vaultQueryBy<ContractState>(criteria).states.size

        return results
}
*/


    @GET
    @Path("transactions2")
    @Produces(MediaType.APPLICATION_JSON)
    fun gettransatcions2(): List<StateAndRef<ContractState>> {
        // Filter by state type: IOU.


        val TODAY = Instant.now()

        ///val start = TODAY
        //val end = TODAY.plus(30, ChronoUnit.DAYS)

        val pagingSpec = PageSpecification(DEFAULT_PAGE_NUM, 100)
        val start = TODAY.minus(1, ChronoUnit.HOURS)
        val end = TODAY.plus(1, ChronoUnit.HOURS)


        val recordedBetweenExpression = QueryCriteria.TimeCondition(
                QueryCriteria.TimeInstantType.RECORDED,
                ColumnPredicate.Between(start, end))

        val criteria = QueryCriteria.VaultQueryCriteria(timeCondition = recordedBetweenExpression, status = Vault.StateStatus.ALL)
        val results = rpcOps.vaultQueryBy<ContractState>(criteria, paging = pagingSpec)
        val size = results.states.count()

        val results2 = rpcOps.vaultQueryBy<ContractState>(criteria, paging = pagingSpec).states.size
        //<-----

        var states = rpcOps.vaultQueryBy<ContractState>().states

        if (states != null && states.size > 0) {
            var aa = 10
            var test = states[0].state.data.javaClass.name

            aa++;
        }
        //     //TODO: for test
        //result = net.corda.training.state.IOUState
        //       var className =   IOUState::javaClass.name
        val result = states.stream()
                // Convert to steam
                .filter({ x -> "net.corda.training.state.IOUState" == x.state.data.javaClass.name })
                .collect(Collectors.toList())// we want "jack" only
        // If 'findAny' then return found

        val result3 = states.stream()
                // Convert to steam
                .filter({ x -> "net.corda.training.state.IOUState" == x.state.data.javaClass.name })
                .count()

        return states
        //.map { it-> it.state.contract.filter {  } "" }
    }


    @GET
    @Path("transactions")
    @Produces(MediaType.APPLICATION_JSON)
    fun gettransatcions(): List<StateAndRef<ContractState>> {

        val TODAY = Instant.now()
        val pagingSpec = PageSpecification(DEFAULT_PAGE_NUM, 100)
        val start = TODAY.minus(1, ChronoUnit.HOURS)
        val end = TODAY.plus(1, ChronoUnit.HOURS)

        val recordedBetweenExpression = QueryCriteria.TimeCondition(
                QueryCriteria.TimeInstantType.RECORDED,
                ColumnPredicate.Between(start, end))

        val criteria = QueryCriteria.VaultQueryCriteria(timeCondition = recordedBetweenExpression, status = Vault.StateStatus.ALL)
        val results = rpcOps.vaultQueryBy<ContractState>(criteria, paging = pagingSpec)
        val size = results.states.count()

        return rpcOps.vaultQueryBy<ContractState>().states
    }

    @GET
    @Path("transactions4")
    @Produces(MediaType.APPLICATION_JSON)
    fun gettransatcions4(): Int {

        val transactions = rpcOps.internalVerifiedTransactionsSnapshot()

        val size = transactions.size

        return size
    }

    @GET
    @Path("get-transaction-count")
    fun getTransactionsCount(@QueryParam(value = "startDate") start: String,
                             @QueryParam(value = "endDate") end: String): Response {

        return try {
            var startPeriod = 0L
            var endPeriod = 0L

            var count = 0

            if (start != null && end != null) {
                try {
                    startPeriod = java.lang.Long.parseLong(start)
                    endPeriod = java.lang.Long.parseLong(end)

                    if (startPeriod > endPeriod) {
                        val temp = startPeriod
                        startPeriod = endPeriod
                        endPeriod = temp
                    }

                    val flowFuture = rpcOps.startTrackedFlow(::QueryTransactionCountFlowWithRange, startPeriod, endPeriod)
                    count = flowFuture.returnValue.getOrThrow()

                } catch (e: NumberFormatException) {

                }
            } else {
                //  val flowHandle = rpcOps.startFlow(::QueryTokenValueListFlow,startDate,endDate)//.returnValue.get()
                val flowFuture = rpcOps.startTrackedFlow(::QueryTransactionCountFlow)
                count = flowFuture.returnValue.getOrThrow()
            }

            Response.status(Response.Status.CREATED).entity("There are $count transaction at node").build()

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.status(Response.Status.BAD_REQUEST).entity(ex.message!!).build()
        }
    }


    /**
     * Displays all cash states that exist in the node's vault.
     */
    @GET
    @Path("cash")
    @Produces(MediaType.APPLICATION_JSON)
    fun getCash(): List<StateAndRef<ContractState>> {
        // Filter by state type: Cash.
        return rpcOps.vaultQueryBy<Cash.State>().states
    }

    /**
     * Displays all cash states that exist in the node's vault.
     */
    @GET
    @Path("cash-balances")
    @Produces(MediaType.APPLICATION_JSON)
    // Display cash balances.
    fun getCashBalances() = rpcOps.getCashBalances()

    /**
     * Initiates a flow to agree an IOU between two parties.
     * Example request:
     * curl -X PUT 'http://localhost:10007/api/iou/issue-iou?amount=99&currency=GBP&party=O=ParticipantC,L=New York,C=US
     */
    @PUT
    @Path("issue-iou")
    fun issueIOU(@QueryParam(value = "amount") amount: Int,
                 @QueryParam(value = "currency") currency: String,
                 @QueryParam(value = "party") party: String): Response {
        // Get party objects for myself and the counterparty.
        val me = rpcOps.nodeInfo().legalIdentities.first()
        val lender = rpcOps.wellKnownPartyFromX500Name(CordaX500Name.parse(party))
                ?: throw IllegalArgumentException("Unknown party name.")
        // Create a new IOU state using the parameters given.
        try {
            /// val state = IOUState(Amount(amount.toLong() * 100, Currency.getInstance(currency)), lender, me)
            // Start the IOUIssueFlow. We block and waits for the flow to return.
            val result = rpcOps.startTrackedFlow(::IOUIssueFlow, amount, currency, lender).returnValue.get()
            //--  before
            //--rpcOps.startTrackedFlow(::IOUIssueFlow, state).returnValue.get()


            // Return the response.
            return Response
                    .status(Response.Status.CREATED)
                    .entity("Transaction id ${result.id} committed to ledger.\n${result.tx.outputs.single()}")
                    .build()
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (e: Exception) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.message)
                    .build()
        }
    }

    /**
     * Transfers an IOU specified by [linearId] to a new party.
     * //http://localhost:10007/api/iou/transfer-iou?id=c35f5588-3c66-44e6-bf7d-2cc87aba8590&party=C=US,L=New York,O=ParticipantC
    //http://localhost:10007/api/iou/transfer-iou?id=05714170-b0ac-4fb6-9e76-b783b391e195&party=O=ParticipantA,%20L=London,%20C=GB
     */
    @GET
    @Path("transfer-iou")
    fun transferIOU(@QueryParam(value = "id") id: String,
                    @QueryParam(value = "party") party: String): Response {
        val linearId = UniqueIdentifier.fromString(id)
        val newLender = rpcOps.wellKnownPartyFromX500Name(CordaX500Name.parse(party))
                ?: throw IllegalArgumentException("Unknown party name.")

        try {
            rpcOps.startFlow(::IOUTransferFlow, linearId, newLender).returnValue.get()
            return Response.status(Response.Status.CREATED).entity("IOU $id transferred to $party.").build()

        } catch (e: Exception) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.message)
                    .build()
        }
    }

    /**
     * Settles an IOU. Requires cash in the right currency to be able to settle.
    http://localhost:10007/api/iou/settle-iou?id=64f3271c-19fe-4514-8662-7438f20215d9&amount=1&currency=USD
     */
    @GET
    @Path("settle-iou")
    fun settleIOU(@QueryParam(value = "id") id: String,
                  @QueryParam(value = "amount") amount: Int,
                  @QueryParam(value = "currency") currency: String): Response {
        val linearId = UniqueIdentifier.fromString(id)
        val settleAmount = Amount(amount.toLong() * 100, Currency.getInstance(currency))

        try {
            rpcOps.startFlow(::IOUSettleFlow, linearId, settleAmount).returnValue.get()
            return Response.status(Response.Status.CREATED).entity("$amount $currency paid off on IOU id $id.").build()

        } catch (e: Exception) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.message)
                    .build()
        }
    }

    /**
     * Helper end-point to issue some cash to ourselves.
    //http://localhost:10013/api/iou/self-issue-cash?amount=100000&currency=USD
     */
    @GET
    @Path("self-issue-cash")
    fun selfIssueCash(@QueryParam(value = "amount") amount: Int,
                      @QueryParam(value = "currency") currency: String): Response {
        val issueAmount = Amount(amount.toLong() * 100, Currency.getInstance(currency))

        try {
            val cashState = rpcOps.startFlow(::SelfIssueCashFlow, issueAmount).returnValue.get()
            return Response.status(Response.Status.CREATED).entity(cashState.toString()).build()

        } catch (e: Exception) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.message)
                    .build()
        }
    }
}