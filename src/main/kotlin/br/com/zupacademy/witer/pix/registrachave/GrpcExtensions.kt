package br.com.zupacademy.witer.pix

import br.com.zupacademy.witer.RegistraChavePixRequest
import br.com.zupacademy.witer.TipoChave.*
import br.com.zupacademy.witer.TipoConta.*
import br.com.zupacademy.witer.pix.registrachave.NovaChavePix


fun RegistraChavePixRequest.paraNovaChavePix(): NovaChavePix {
    return NovaChavePix(
        clienteId = clienteId,
        tipoChavePix = when (tipoChave) {
            TIPO_CHAVE_DESCONHECIDA -> null
            else -> TipoChave.valueOf(tipoChave.name)
        },
        chave = chave,
        tipoContaPix = when (tipoConta) {
            TIPO_CONTA_DESCONHECIDA -> null
            else -> TipoConta.valueOf(tipoConta.name)
        }
    )
}