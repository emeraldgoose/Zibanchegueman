package com.zibanchegueman.contract

import com.zibanchegueman.contract.IOUContract.Companion.ID
import com.zibanchegueman.state.IOUState
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class IOUContractTests {
    private val ledgerServices = MockServices()
    private val megaCorp = TestIdentity(CordaX500Name("MegaCorp", "London", "GB"))
    private val miniCorp = TestIdentity(CordaX500Name("MiniCorp", "New York", "US"))
    private val iouValue = 1
    private val from = "sejong"
    private val to = "GS25"
    private val date = "20191111"
    private val type = "Widthdraw"

    @Test
    fun `transaction must include Create command`() {
        ledgerServices.ledger {
            transaction {
                output(ID, IOUState(miniCorp.party, megaCorp.party,from,to,iouValue,date,type))
                fails()
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), IOUContract.Commands.Create())
                verifies()
            }
        }
    }

    @Test
    fun `transaction must have no inputs`() {
        ledgerServices.ledger {
            transaction {
                input(ID, IOUState(miniCorp.party, megaCorp.party,from,to,iouValue,date,type))
                output(ID, IOUState(miniCorp.party, megaCorp.party,from,to,iouValue,date,type))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), IOUContract.Commands.Create())
                `fails with`("No inputs should be consumed when issuing an IOU.")
            }
        }
    }

    @Test
    fun `transaction must have one output`() {
        ledgerServices.ledger {
            transaction {
                output(ID, IOUState(miniCorp.party, megaCorp.party,from,to,iouValue,date,type))
                output(ID, IOUState(miniCorp.party, megaCorp.party,from,to,iouValue,date,type))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), IOUContract.Commands.Create())
                `fails with`("Only one output state should be created.")
            }
        }
    }

    @Test
    fun `lender must sign transaction`() {
        ledgerServices.ledger {
            transaction {
                output(ID, IOUState(miniCorp.party, megaCorp.party,from,to,iouValue,date,type))
                command(miniCorp.publicKey, IOUContract.Commands.Create())
                `fails with`("All of the participants must be signers.")
            }
        }
    }

    @Test
    fun `borrower must sign transaction`() {
        ledgerServices.ledger {
            transaction {
                output(ID, IOUState(miniCorp.party, megaCorp.party,from,to,iouValue,date,type))
                command(megaCorp.publicKey, IOUContract.Commands.Create())
                `fails with`("All of the participants must be signers.")
            }
        }
    }

    @Test
    fun `lender is not borrower`() {
        ledgerServices.ledger {
            transaction {
                output(ID, IOUState(miniCorp.party, megaCorp.party,from,to,iouValue,date,type))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), IOUContract.Commands.Create())
                `fails with`("The lender and the borrower cannot be the same entity.")
            }
        }
    }

    @Test
    fun `cannot create negative-value IOUs`() {
        ledgerServices.ledger {
            transaction {
                output(ID, IOUState(miniCorp.party, megaCorp.party,from,to,-1,date,type))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), IOUContract.Commands.Create())
                `fails with`("The IOU's value must be non-negative.")
            }
        }
    }
}