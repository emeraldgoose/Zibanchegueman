package com.zibanchegueman.contract

import com.zibanchegueman.state.IOUState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

/**
 * A implementation of a basic smart contract in Corda.
 *
 * This contract enforces rules regarding the creation of a valid [IOUState], which in turn encapsulates an [IOU].
 *
 * For a new [IOU] to be issued onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output state: the new [IOU].
 * - An Create() command with the public keys of both the lender and the borrower.
 *
 * All contracts must sub-class the [Contract] interface.
 */
class IOUContract : Contract {
    companion object {
        const val ID = "com.zibanchegueman.contract.IOUContract"
    }

    override fun verify(tx: LedgerTransaction) {

        /**
         * requireSingleCommand 를 통해 create 명령이 있는지 확인합니다.
         * tx.inputs : 입력을 나열합니다.
         * tx.outputs : 출력을 나열합니다.
         * tx.commands : 명령 및 관련 서명자를 나열합니다.
         */

        val command = tx.commands.requireSingleCommand<Commands.Create>()

        requireThat {
            // Constraints on the shape of the transaction.
            "No inputs should be consumed when issuing an IOU." using (tx.inputs.isEmpty())
            "There should be one output state of type IOUState." using (tx.outputs.size == 1)

            // IOU-specific constraints.
            val output = tx.outputsOfType<IOUState>().single()
            "The IOU's value must be non-negative." using (output.value > 0)
            "The affiliation and the vaultServer cannot be the same entity." using (output.affiliation != output.peer)

            // Constraints on the signers.
            val expectedSigners = listOf(output.peer.owningKey, output.affiliation.owningKey)
            "There must be two signers." using (command.signers.toSet().size == 2)
            "The vaultServer and affiliation must be signers." using (command.signers.containsAll(expectedSigners))
        }
    }

    /**
     * This contract only implements one command, Create.
     */
    interface Commands : CommandData {
        class Create : Commands
    }
}
