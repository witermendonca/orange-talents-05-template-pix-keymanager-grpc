package br.com.zupacademy.witer.pix.carregachave

import br.com.zupacademy.witer.CarregaChavePixResponse
import br.com.zupacademy.witer.TipoChave
import br.com.zupacademy.witer.TipoConta
import com.google.protobuf.Timestamp
import java.time.ZoneId

class CarregaChavePixResponseConverter {

    fun convert(chaveInfo: ChavePixInfo): CarregaChavePixResponse {
        return CarregaChavePixResponse.newBuilder()
            .setClienteId(chaveInfo.clienteId?.toString() ?: "")
            .setPixId(chaveInfo.pixId?.toString() ?: "")
            .setChavePix(CarregaChavePixResponse.ChavePix
                .newBuilder()
                .setTipo(TipoChave.valueOf(chaveInfo.tipochave.name))
                .setChave(chaveInfo.chave)
                .setConta(CarregaChavePixResponse.ChavePix.ContaInfo.newBuilder()
                    .setTipo(TipoConta.valueOf(chaveInfo.tipoConta.name))
                    .setInstituicao(chaveInfo.conta.instituicao)
                    .setNomeDoTitular(chaveInfo.conta.nomeDoTitular)
                    .setCpfDoTitular(chaveInfo.conta.cpfDoTitular)
                    .setAgencia(chaveInfo.conta.agencia)
                    .setNumeroDaConta(chaveInfo.conta.numeroDaConta)
                    .build()
                )
                .setCriadaEm(chaveInfo.registradaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
            )
            .build()
    }

}