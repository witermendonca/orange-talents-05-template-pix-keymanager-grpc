package br.com.zupacademy.witer.pix.removechave

import br.com.zupacademy.witer.compartilhado.validator.ValidUUID
import br.com.zupacademy.witer.exceptions.ChavePixNaoEncontradaException
import br.com.zupacademy.witer.externo.bcb.BancoCentralClient
import br.com.zupacademy.witer.externo.bcb.DeletePixKeyBcbRequest
import br.com.zupacademy.witer.pix.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Singleton
@Validated
class RemoveChavePixService(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val bancoCentralClient: BancoCentralClient,
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    fun remove(
        @NotBlank @ValidUUID clienteId: String?,
        @NotBlank @ValidUUID chavePixId: String?,
    ) {

        //Busca chavePix pelo clienteId e chavePixId
        val chavePix = chavePixRepository.findByIdAndClienteId(UUID.fromString(chavePixId),
            UUID.fromString(clienteId)).orElseThrow {
            logger.error("Chave Pix não encontrada.")
            ChavePixNaoEncontradaException("Chave Pix não encontrada.")
        }

        //Deleta chavePix
        chavePixRepository.delete(chavePix)

        //Tenta deletar chavePix no Banco Central
        val bcbResponse =
            bancoCentralClient.deletePixKeyBcb(key = chavePix.chave, request = DeletePixKeyBcbRequest(chavePix.chave))

        //Verifica StatusResponse BCB da tentativa de deletar chavePix.
        if (bcbResponse.status != HttpStatus.OK) {
            logger.error("Erro ao remover chave Pix no Banco Central do Brasil (BCB)")
            throw IllegalStateException("Erro ao remover chave Pix no Banco Central do Brasil (BCB)")
        }
    }
}