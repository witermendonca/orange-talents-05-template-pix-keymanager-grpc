package br.com.zupacademy.witer.pix.removechave

import br.com.zupacademy.witer.compartilhado.validator.ValidUUID
import br.com.zupacademy.witer.exceptions.ChavePixNaoEncontradaException
import br.com.zupacademy.witer.pix.ChavePixRepository
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotBlank

@Singleton
@Validated
class RemoveChavePixService(
    @Inject val chavePixRepository: ChavePixRepository,
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun remove(
        @NotBlank @ValidUUID clienteId: String?, // 1
        @NotBlank @ValidUUID chavePixId: String?,
    ) {

        val chave = chavePixRepository.findByIdAndClienteId(UUID.fromString(chavePixId),
            UUID.fromString(clienteId)).orElseThrow {
            logger.error("Chave Pix não encontrada.")
            ChavePixNaoEncontradaException("Chave Pix não encontrada.")
        }

        chavePixRepository.delete(chave)

    }
}