package com.zibanchegueman.state

import com.zibanchegueman.schema.IOUSchemaV1
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

/**
 * State : affiliation : 감사 소속 기관 or 은행 기관
 * vaultServer : 거래 내역이 저장된 블록체인이 저장될 서버 = 금고서버
 * from, to & value : 거래 대상자와 거래금액
 * date : 거래가 된 시간
 * type : 입금(deposit) or 출금(withdraw)
*/
data class IOUState(val affiliation: Party,
                    val peer: Party,
                    val from: String,
                    val to: String,
                    val value: Int,
                    val date: String,
                    var type: String,
                    override val linearId: UniqueIdentifier = UniqueIdentifier()):
        LinearState, QueryableState {
    /** The public keys of the involved parties. */
    override val participants: List<AbstractParty> get() = listOf(affiliation, peer)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is IOUSchemaV1 -> IOUSchemaV1.PersistentIOU(
                    this.affiliation.name.toString(),
                    this.peer.name.toString(),
                    this.value,
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(IOUSchemaV1)
}
