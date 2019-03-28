package eloc.helpers
/*
import eloc.state.BillOfLadingProperties
import eloc.state.InvoiceProperties
import eloc.state.PackingListProperties
import net.corda.core.crypto.SecureHash
import net.corda.finance.DOLLARS
import java.time.LocalDate
import net.corda.testing.*
*//*
var invoiceProperties =
        InvoiceProperties(
                invoiceID = "123",
                seller = LocDataStructures.Company(
                        name = "Mega Corp LTD.",
                        address = "123 Main St. Awesome Town, ZZ 11111",
                        phone = null
                ),
                buyer = LocDataStructures.Company(
                        name = "Sandworm Imports",
                        address = "555 Elm St. Little Town, VV, 22222",
                        phone = null
                ),
                invoiceDate = LocalDate.now(),
                attachmentHash =  SecureHash.SHA256(ByteArray(32, { 0.toByte() })),
                term = 60,
                goods = arrayListOf(
                        LocDataStructures.PricedGood(
                                description = "Salt",
                                purchaseOrderRef = null,
                                quantity = 10,
                                unitPrice = 3.DOLLARS,
                                grossWeight = null
                        ),
                        LocDataStructures.PricedGood(
                                description = "Pepper",
                                purchaseOrderRef = null,
                                quantity = 20,
                                unitPrice = 4.DOLLARS,
                                grossWeight = null
                        )
                )

        )
val plProperties = PackingListProperties(
        issueDate = LocalDate.now(),
        orderNumber = "123",
        sellersOrderNumber = "123",
        transportMethod = "Ship",
        nameOfVessel = "SurfRider",
        billOfLadingNumber = "123",
        seller = LocDataStructures.Company(
                name = "Mega Corp LTD.",
                address = "123 Main St. Awesome Town, ZZ 11111",
                phone = null
        ),
        buyer = LocDataStructures.Company(
                name = "Sandworm Imports",
                address = "555 Elm St. Little Town, VV, 22222",
                phone = null
        ),
        descriptionOfGoods = arrayListOf(
                LocDataStructures.PricedGood(
                        description = "Salt",
                        purchaseOrderRef = null,
                        quantity = 10,
                        unitPrice = 3.DOLLARS,
                        grossWeight = null
                ),
                LocDataStructures.PricedGood(
                        description = "Pepper",
                        purchaseOrderRef = null,
                        quantity = 20,
                        unitPrice = 4.DOLLARS,
                        grossWeight = null
                )
        ),
        attachmentHash =  SecureHash.SHA256(ByteArray(32, { 0.toByte() }))
)

val bolProperties = BillOfLadingProperties(
        billOfLadingID = "billOfLadingID",
        issueDate = LocalDate.now(),
        carrierOwner = MINI_CORP,
        nameOfVessel = "Karaboudjan",
        descriptionOfGoods = listOf(LocDataStructures.Good(description="Crab meet cans",quantity = 10000,grossWeight = null)),
        dateOfShipment = LocalDate.now(),
        portOfLoading = LocDataStructures.Port(country = "Morokko",city = "Larache",address = null,state = null,name=null),
        portOfDischarge = LocDataStructures.Port(country = "Belgium",city = "Antwerpen",address = null,state = null,name=null),
        shipper = null,
        notify = LocDataStructures.Person(
                name = "Some guy",
                address = "Some address",
                phone = "+11 23456789"
        ),
        consignee = LocDataStructures.Company(
                name = "Some company",
                address = "Some other address",
                phone = "+11 12345678"
        ),
        grossWeight = LocDataStructures.Weight(
                quantity =  2500.0,
                unit = LocDataStructures.WeightUnit.KG
        ),
        placeOfReceipt = LocDataStructures.Location(
                country = "USA",
                state ="Iowa",
                city = "Des Moines"

        )
)*/