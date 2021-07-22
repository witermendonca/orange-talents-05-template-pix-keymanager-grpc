package br.com.zupacademy.witer.pix.carregachave

import br.com.zupacademy.witer.pix.ChavePix
import br.com.zupacademy.witer.pix.ContaAssociada
import br.com.zupacademy.witer.pix.TipoChave
import br.com.zupacademy.witer.pix.TipoConta
import java.time.LocalDateTime
import java.util.*

data class ChavePixInfo(
    val pixId: UUID? = null,
    val clienteId: UUID? = null,
    val tipochave: TipoChave,
    val chave: String,
    val tipoConta: TipoConta,
    val conta: ContaAssociada,
    val registradaEm: LocalDateTime = LocalDateTime.now(),
) {

    companion object {
        fun of(chavePix: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                pixId = chavePix.id,
                clienteId = chavePix.clienteId,
                tipochave = chavePix.tipoChave,
                chave = chavePix.chave,
                tipoConta = chavePix.tipoConta,
                conta = chavePix.conta,
                registradaEm = chavePix.criadaEm
            )

        }
    }
}
