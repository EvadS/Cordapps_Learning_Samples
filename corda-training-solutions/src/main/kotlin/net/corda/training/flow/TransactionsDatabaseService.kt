package net.corda.training.flow


import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService
import net.corda.training.DatabaseService


val TABLE_NAME = "VAULT_STATES"


val EXCLUDED_STATE = listOf("net.corda.yo.YoState")

val VAULT_STATES_TABLE_NAME = "VAULT_STATES"
val VAULT_STATES_ID = "TRANSACTION_ID"

val NODE_TRANSACTIONS_TABLE_NAME = "NODE_TRANSACTIONS"
val NODE_TRANSACTIONS_ID = "TX_ID"
val START_DATE = "'1970-01-01'"

val DATE_COLUMN_NAME = "RECORDED_TIMESTAMP"
val COUNT_COLUMN_NAME = "Count"
val CONTRACT_STATE_CLASS_NAME = "CONTRACT_STATE_CLASS_NAME"



/**
 * A database service subclass for handling a table of crypto values.
 *
 * @param services The node's service hub.
 */
@CordaService
class TransactionsDatabaseService(services: ServiceHub) : DatabaseService(services) {
    init {
        setUpStorage()
    }

    /**
     * Adds a crypto token and associated value to the table of crypto values.
     */
    fun addTokenValue(token: String, value: Int) {
        val query = "insert into $TABLE_NAME values(?, ?)"



        val params = mapOf(1 to token, 2 to value)

        executeUpdate(query, params)
        log.info("Token $token added to crypto_values table.")
    }

    /**
     * Updates the value of a crypto token in the table of crypto values.
     */
    fun updateTokenValue(token: String, value: Int) {
        val query = "update $TABLE_NAME set value = ? where token = ?"

        val params = mapOf(1 to value, 2 to token)

        executeUpdate(query, params)
        log.info("Token $token updated in crypto_values table.")
    }

    /**
     * Retrieves the value of a crypto token in the table of crypto values.
     */
    fun queryTokenValue(token: String): Int {
        val query = "select value from $TABLE_NAME where token = ?"

        val params = mapOf(1 to token)

        val results = executeQuery(query, params, { it -> it.getInt("value") })

        if (results.isEmpty()) {
            throw IllegalArgumentException("Token $token not present in database.")
        }

        val value = results.single()
        log.info("Token $token read from crypto_values table.")
        return value
    }


    /**
     * Retrieves the value of a crypto token in the table of crypto values.
     */
    fun queryTokenListValue(): List<Int> {
        val query = "select value from $TABLE_NAME "


        val results = executeSelectQuery(query, { it -> it.getInt("value") })

        if (results.isEmpty()) {
            throw IllegalArgumentException("not present any token  in database.")
        }

        System.out.println("=== > Token list : " + results.toString());
        log.info("Token read from crypto_values table.")
        return results
    }

    fun queryCountValueFromRange(startDate: Long, endDate: Long): Int {

        val excludedStateStr = EXCLUDED_STATE.joinToString("\',\'", prefix = "(\'", postfix = "\')")
        val query =

                "SELECT COUNT($NODE_TRANSACTIONS_TABLE_NAME.$NODE_TRANSACTIONS_ID) as $COUNT_COLUMN_NAME\n" +
                        "FROM $NODE_TRANSACTIONS_TABLE_NAME\n" +
                        "JOIN\n" +
                        "  (SELECT $VAULT_STATES_ID AS $VAULT_STATES_ID,\n" +
                        "          MAX(RECORDED_TIMESTAMP) AS RECORDED_TIMESTAMP\n" +
                        "   FROM VAULT_STATES\n" +
                        "   WHERE VAULT_STATES.CONTRACT_STATE_CLASS_NAME NOT IN $excludedStateStr\n" +
                        "    AND  $DATE_COLUMN_NAME between DATEADD('SECOND', $startDate, DATE $START_DATE) and DATEADD('SECOND', $endDate , DATE $START_DATE) "+
        "   GROUP BY $VAULT_STATES_ID) vtx" +
        " ON $NODE_TRANSACTIONS_TABLE_NAME.$NODE_TRANSACTIONS_ID = vtx.$VAULT_STATES_ID"

        println("****** Query --> $query")
        val results = executeSelectQuery(query, { it -> it.getInt(COUNT_COLUMN_NAME) })

        var value = 0
        if (!results.isEmpty()) {
            value = results.single()
        }

        return value
    }

    fun queryCountValue(): Int {
        val excludedStateStr = EXCLUDED_STATE.joinToString("\',\'", prefix = "(\'", postfix = "\')")
        val query =

                "SELECT COUNT($NODE_TRANSACTIONS_TABLE_NAME.$NODE_TRANSACTIONS_ID) as $COUNT_COLUMN_NAME\n" +
                        "FROM $NODE_TRANSACTIONS_TABLE_NAME\n" +
                        "JOIN\n" +
                        "  (SELECT $VAULT_STATES_ID AS $VAULT_STATES_ID,\n" +
                        "          MAX(RECORDED_TIMESTAMP) AS RECORDED_TIMESTAMP\n" +
                        "   FROM VAULT_STATES\n" +
                        "   WHERE VAULT_STATES.CONTRACT_STATE_CLASS_NAME NOT IN $excludedStateStr\n" +
                        "   GROUP BY $VAULT_STATES_ID) vtx" +
                        " ON $NODE_TRANSACTIONS_TABLE_NAME.$NODE_TRANSACTIONS_ID = vtx.$VAULT_STATES_ID"

        val results = executeSelectQuery(query, { it -> it.getInt(COUNT_COLUMN_NAME) })

        println("****** Query --> $query")

        var value = 0
        if (!results.isEmpty()) {
            value = results.single()
        }

        return value
    }

    /**
     * Initialises the table of crypto values.
     */
    private fun setUpStorage() {
        val query = """
            create table if not exists $TABLE_NAME(
                token varchar(64),
                value int
            )"""

        executeUpdate(query, emptyMap())
        log.info("Created crypto_values table.")
    }


}