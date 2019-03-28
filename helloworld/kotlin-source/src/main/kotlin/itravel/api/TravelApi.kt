package itravel.api

import itravel.contract.TravelDataStructures
import itravel.flow.ClaimApprovalFlow
import itravel.flow.FlightTicketFlow
import itravel.flow.LandAcquisitionFlow
import itravel.flow.TicketClaimFlow
import itravel.state.*
import net.corda.core.contracts.Amount
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.slf4j.Logger
import java.time.LocalDate
import java.util.*
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

val SERVICE_NODE_NAMES = listOf(CordaX500Name("Controller", "London", "GB"),
        CordaX500Name("NetworkMapService", "London", "GB"))

@Path("travel")
class TravelApi(val services: CordaRPCOps) {
    private val myLegalName = services.nodeInfo().legalIdentities.first().name

    companion object {
        val logger: Logger = loggerFor<TravelApi>()
    }

    /**
     * Returns the node's name.
     */
    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoami() = mapOf("me" to myLegalName.organisation)

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = services.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                .filter { it != myLegalName && it !in SERVICE_NODE_NAMES })
    }

    /**
     * Get contents of vault
     */
    @GET
    @Path("vault")
    @Produces(MediaType.APPLICATION_JSON)
    fun getVault(): Pair<List<StateAndRef<ContractState>>, List<StateAndRef<ContractState>>> {
        val unconsumedStates = services.vaultQueryBy<ContractState>(QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)).states
        val consumedStates = services.vaultQueryBy<ContractState>(QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED)).states
        return Pair(unconsumedStates, consumedStates)
    }

    @POST
    @Path("create-ticket")
    @Produces(MediaType.APPLICATION_JSON)
    fun createTicket(ticket: FlightTicket): Response {
        //try {
        println("Starting creation of ticket")

        println(ticket)

        val passenger = services.nodeInfo().legalIdentities.first()

        println("Node User Name: " + passenger.name.organisation);
        println("Requested Passenger: " + ticket.passengerName);

        println("passenger.name.organisation.:  " + passenger.name.organisation)
        println("ticket.passengerName" + ticket.passengerName)

        if (!passenger.name.organisation.equals(ticket.passengerName)) {
            println(passenger)
            throw RuntimeException("${ticket.passengerName} not found.")
        }
        println("passenger: " + passenger)
        val airline = services.partiesFromName(ticket.airlineName, exactMatch = false).firstOrNull()

        println("Air Line------->" + airline)
        val flightInsurer = services.partiesFromName(ticket.flightInsurerName, exactMatch = false).firstOrNull()

        if (airline == null) {
            println("Could not find airline")
            throw RuntimeException("$ticket.airlineName not found.")
        }
        println("Airline found")

        if (flightInsurer == null) {
            println("Could not find flight Insurer")
            throw RuntimeException("$ticket.flightInsurerName not found.")
        }

        println("Flight Insurer found")

        println("********************* Data Start *****************************")
        println(ticket.ticketNo)
        println(ticket.passportNo)
        println(LocalDate.parse(ticket.passportIssueDate))
        println(LocalDate.parse(ticket.passportExpiryDate))
        println(ticket.passengerName)
        println(ticket.passengerCount)
        println(ticket.amount.toLong())
        println(ticket.currency)
        println(SecureHash.randomSHA256())

        println("********************* Data End *****************************")

        val travel = TravelProperties(
                ticketNo = ticket.ticketNo,
                passport = TravelDataStructures.Passport(ticket.passportNo, LocalDate.parse(ticket.passportIssueDate), LocalDate.parse(ticket.passportExpiryDate)),
                passengerName = ticket.passengerName,
                passengerCount = ticket.passengerCount,
                amount = Amount(ticket.amount.toLong(), Currency.getInstance(ticket.currency)),
                attachmentHash = SecureHash.randomSHA256()
        )

        println("Flight Ticket Data Created")

        val state = FlightTicketState(passenger, airline, flightInsurer, "Open", travel)
        if (state.status.equals("Open")) println("Travel State created")

        println("Starting flow")
        val result = services.startFlow(FlightTicketFlow::CreateTicket, airline, flightInsurer, state)
                .returnValue
                .getOrThrow()
        println("Ending flow")

        println("Transaction: " + result.tx.id);

        /*} catch (e: Exception) {
            println("Exception handled----------"+e)
        }*/
        val respData = "{\"transactionId\":\"${result.tx.id}\"}";

        return Response.accepted().entity(respData).build()

    }

    @GET
    @Path("/test")
    fun test() {
        var test = FlightTicket(
                "1", "111", "2012-10-10", "2020-12-12",

                "passengerName",
                1,
                100,
        "USD",
        "airlineName",
        "flightInsurerName")

        println("-------")
        println("json" + test.toString())

    }


    @POST
    @Path("create-claims")
    @Produces(MediaType.APPLICATION_JSON)
    fun createClaim(claim: TicketClaim): Response {

        println("Starting creation of claim")

        println(claim)

        val passenger = services.nodeInfo().legalIdentities.first()

        println("Node User Name: " + passenger.name.organisation);
        println("Requested Passenger: " + claim.passengerName);

        if (!passenger.name.organisation.equals(claim.passengerName)) {
            println("passenger Name not found" + passenger)
            throw RuntimeException("${claim.passengerName} not found.")
        }
        println("Air line Name------->" + claim.airlineName)
        println("passenger: " + passenger)
        val airline = services.partiesFromName(claim.airlineName, exactMatch = false).firstOrNull()

        println("Air Line------->" + airline)
        val flightInsurer = services.partiesFromName(claim.flightInsurerName, exactMatch = false).firstOrNull()
        println("Flight Insurer------->" + flightInsurer)

        if (airline == null) {
            println("Could not find airline")
            throw RuntimeException("$claim.airlineName not found.")
        }
        println("Airline found")

        if (flightInsurer == null) {
            println("Could not find flight Insurer")
            throw RuntimeException("$claim.flightInsurerName not found.")
        }

        println("Flight Insurer found")

        println("********************* Data Start *****************************")
        println(claim.flightTicketNo)
        println(LocalDate.parse(claim.claimDate))
        println(claim.passengerName)
        println(claim.claimAmount.toLong())
        println(claim.currency)
        println(claim.flightTicketTx)
        println(SecureHash.randomSHA256())

        val ticketClaimReference = services.vaultQueryBy<FlightTicketState>().states.filter {
            it.ref.txhash.toString().equals(claim.flightTicketTx)
        }.first()

        println("stateAndRef------------>" + ticketClaimReference)
        println("********************* Data End *****************************")

        val claim = TicketClaimProperties(
                flightTicketNo = claim.flightTicketNo,
                passengerName = claim.passengerName,
                claimDate = LocalDate.parse(claim.claimDate),
                claimAmount = Amount(claim.claimAmount.toLong(), Currency.getInstance(claim.currency)),
                ticketClaimRef = ticketClaimReference.ref,
                attachmentHash = SecureHash.randomSHA256()
        )

        println("Flight Ticket Claim Data Created")

        val state = TicketClaimState(passenger, airline, flightInsurer, "Open", claim)
        if (state.status.equals("Open")) println("Ticket Claim State created")

        println("Starting flow")
        val result = services.startFlow(TicketClaimFlow::CreateTicketClaim, state)
                .returnValue
                .getOrThrow()
        println("Ending flow")

        println("Transaction: " + result.tx.id);

        val respData = "{\"transactionId\":\"${result.tx.id}\"}";

        return Response.accepted().entity(respData).build()
    }


    @POST
    @Path("sell-land-first")
    @Produces(MediaType.APPLICATION_JSON)
    fun landSell(): Response {

        println("Starting creation of land")

        val regulatory = services.nodeInfo().legalIdentities.first()
        println("Node User Name: " + regulatory.name.organisation);
        val buyerPartyA = "partyA"
        val buyer = services.partiesFromName(buyerPartyA, exactMatch = false).first()
        println("buyer found")
        println("********************* Data Start *****************************")
        println(SecureHash.randomSHA256())
        println("********************* Data End *****************************")

        println(" Data Created")
        val state = LandAcquisition(regulatory, regulatory, buyer, "open")
        if (state.status.equals("Open")) println("Ticket Claim State created")

        println("Starting flow")
        val result = services.startFlow(LandAcquisitionFlow::sellByRegulatory, state)
                .returnValue
                .getOrThrow()
        println("Ending flow")

        println("Transaction: " + result.tx.id);

        val respData = "{\"transactionId\":\"${result.tx.id}\"}";

        return Response.accepted().entity(respData).build()
    }

    @GET
    @Path("sell-land-second")
    @Produces(MediaType.APPLICATION_JSON)
    fun landSellS(@QueryParam(value = "ref") ref: String): Response {

        println("Starting creation of land")

        val regulatory = services.nodeInfo().legalIdentities.first()
        println("Node User Name: " + regulatory.name.organisation);
        val partyA = "partyA"
        val seller = services.partiesFromName(partyA, exactMatch = false).first()
        val partyB = "partyB"
        val buyer = services.partiesFromName(partyB, exactMatch = false).first()
        println("buyer found")
        println("********************* Data Start *****************************")
        println(SecureHash.randomSHA256())

        println("stateAndRef------------>" + ref)
        println("********************* Data End *****************************")
        println("********************* Data End *****************************")

        println(" Data Created")
        val stateAndRef = services.vaultQueryBy<LandAcquisition>().states.filter {
            it.ref.txhash.toString().equals(ref)
        }.first()
        println("Starting flow")
        val result = services.startFlow(LandAcquisitionFlow::sellByPartyA, stateAndRef.ref, seller, buyer)
                .returnValue
                .getOrThrow()
        println("Ending flow")

        println("Transaction: " + result.tx.id);

        val respData = "{\"transactionId\":\"${result.tx.id}\"}";

        return Response.accepted().entity(respData).build()
    }


    @GET
    @Path("airline-approval")
    @Produces(MediaType.APPLICATION_JSON)
    fun approveClaimByAirline(@QueryParam(value = "ref") ref: String): Response {

        val airline = services.nodeInfo().legalIdentities.first()

        println("Node User Name----: " + airline.name.organisation);

        val stateAndRef = services.vaultQueryBy<TicketClaimState>().states.filter {
            it.ref.txhash.toString().equals(ref)
        }.first()

        if (!airline.name.organisation.equals(stateAndRef.state.data.airline.name.organisation)) {
            println("Submit by airline only-" + airline)
            throw RuntimeException("${stateAndRef.state.data.airline.name.organisation} not found.")
        }
        println("Starting flow")
        val result = services.startFlow(ClaimApprovalFlow::ApproveByAirline, stateAndRef.ref)
                .returnValue
                .getOrThrow()
        println("Ending flow")

        println("Transaction: " + result.tx.id);

        val respData = "{\"transactionId\":\"${result.tx.id}\"}";

        return Response.accepted().entity(respData).build()
    }

    @GET
    @Path("insurer-approval")
    @Produces(MediaType.APPLICATION_JSON)
    fun approveClaimByInsurer(@QueryParam(value = "ref") ref: String): Response {

        val insurer = services.nodeInfo().legalIdentities.first()

        println("Node User Name----: " + insurer.name.organisation);
        val stateAndRef = services.vaultQueryBy<TicketClaimState>().states.filter {
            it.ref.txhash.toString().equals(ref)
        }.first()

        if (!insurer.name.organisation.equals(stateAndRef.state.data.flightInsurer.name.organisation)) {
            println("Submit by Insurer only-" + insurer)
            throw RuntimeException("${stateAndRef.state.data.flightInsurer.name.organisation} not found.")
        }
        println("Starting flow")
        val result = services.startFlow(ClaimApprovalFlow::ApproveByInsurer, stateAndRef.ref)
                .returnValue
                .getOrThrow()
        println("Ending flow")

        println("Transaction: " + result.tx.id);

        val respData = "{\"transactionId\":\"${result.tx.id}\"}";

        return Response.accepted().entity(respData).build()
    }

    /**
     * Fetches packing list state that matches ref from the node's vault.
     */
    @GET
    @Path("get-claims-by-status")
    @Produces(MediaType.APPLICATION_JSON)
    fun getClaimsByStatus(@QueryParam(value = "status") status: String): List<Pair<String, ClaimSummary>>? {
        println("fetching claims list with status $status")
        return listClaims(status)
    }

    private fun listClaims(status: String): List<Pair<String, ClaimSummary>> {
        val states = services.vaultQueryBy<TicketClaimState>().states
        println("listClaims-----1---" + states)
        return states.filter {
            println("fetching claims list with status----22--- $it.state.data.status == $status")
            it.state.data.status == status
        }.map {
            Pair(it.ref.toString(),
                    ClaimSummary(it.state.data.props.flightTicketNo,
                            it.state.data.props.ticketClaimRef.txhash,
                            it.ref.txhash,
                            it.state.data.props.passengerName,
                            it.state.data.props.claimDate,
                            it.state.data.props.claimAmount,
                            it.state.data.airline.name.organisation,
                            it.state.data.flightInsurer.name.organisation,
                            it.state.data.status))


        }
    }


    /**
     * Displays all flight ticket states that exist in the node's vault.
     */
    @GET
    @Path("flight-tickets")
    @Produces(MediaType.APPLICATION_JSON)
    fun getFlightTickets(): List<StateAndRef<FlightTicketState>> {
        println("fetching flight tickets")
        return services.vaultQueryBy<FlightTicketState>().states;
    }

    /**
     * Displays all ticket claim states that exist in the node's vault.
     */
    @GET
    @Path("ticket-claims")
    @Produces(MediaType.APPLICATION_JSON)
    fun getTicketClaims(): List<StateAndRef<TicketClaimState>> {
        println("fetching ticket claims")
        //return services.vaultQueryBy<TicketClaimState>().states.map { it.state.data }
        return services.vaultQueryBy<TicketClaimState>().states;
    }


    data class Land(val landTx: String
    )

    data class FlightTicket(val ticketNo: String,
                            val passportNo: String,
                            val passportIssueDate: String,
                            val passportExpiryDate: String,
                            val passengerName: String,
                            val passengerCount: Int,
                            val amount: Int,
                            val currency: String,
                            val airlineName: String,
                            val flightInsurerName: String)

    data class TicketClaim(val flightTicketNo: String,
                           val flightTicketTx: String,
                           val passengerName: String,
                           val claimDate: String,
                           val claimAmount: Int,
                           val currency: String,
                           val airlineName: String,
                           val flightInsurerName: String)

    data class ClaimSummary(val flightTicketNo: String,
                            val flightTicketTx: SecureHash,
                            val flightClaimTx: SecureHash,
                            val passengerName: String,
                            val claimDate: LocalDate,
                            val claimAmount: Amount<Currency>,
                            val airlineName: String,
                            val flightInsurerName: String,
                            val status: String)

}