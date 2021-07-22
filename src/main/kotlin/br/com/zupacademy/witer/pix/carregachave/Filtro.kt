package br.com.zupacademy.witer.pix.carregachave

import br.com.zupacademy.witer.compartilhado.validator.ValidUUID
import br.com.zupacademy.witer.exceptions.ChavePixNaoEncontradaException
import br.com.zupacademy.witer.externo.bcb.BancoCentralClient
import br.com.zupacademy.witer.pix.ChavePixRepository
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    /**
     * Deve retornar chave encontrada ou lançar um exceção de erro de chave não encontrada
     */
    abstract fun filtra(repository: ChavePixRepository, bcbClient: BancoCentralClient): ChavePixInfo

    @Introspected
    data class PorPixEClenteId(
        @field: NotBlank @field: ValidUUID val clienteId: String?,
        @field: NotBlank @field: ValidUUID val pixId: String?,
    ) : Filtro() {

        private val logger = LoggerFactory.getLogger(this.javaClass)

        override fun filtra(repository: ChavePixRepository, bcbClient: BancoCentralClient): ChavePixInfo {
            return repository.findById(UUID.fromString(pixId))
                .filter { it.pertenceAo(UUID.fromString(clienteId)) }
                .map(ChavePixInfo::of)
                .orElseThrow {
                    logger.error("Chave Pix Não Encontrada.")
                    ChavePixNaoEncontradaException("Chave Pix Não Encontrada.")
                }
        }
    }

    @Introspected
    data class PorChave(@field: NotBlank @field: Size(max = 77) val chave: String) : Filtro() {

        private val logger = LoggerFactory.getLogger(this.javaClass)

        override fun filtra(repository: ChavePixRepository, bcbClient: BancoCentralClient): ChavePixInfo {
            return repository.findByChave(chave)
                .map(ChavePixInfo::of)
                .orElseGet {
                    logger.info("Consultando chave Pix '$chave' no Banco Central do Brasil (BCB)")
                    val resposta = bcbClient.findByPixKeyBcb(chave)
                    when (resposta.status) {
                        HttpStatus.OK -> resposta.body()?.toModel()
                        else -> throw ChavePixNaoEncontradaException("Chave Pix Não Encontrada.").also {
                            logger.error("Chave Pix Não Encontrada.")
                        }
                    }
                }
        }

    }

    @Introspected
    class Invalido : Filtro() {

        private val logger = LoggerFactory.getLogger(this.javaClass)

        override fun filtra(repository: ChavePixRepository, bcbClient: BancoCentralClient): ChavePixInfo {
            throw IllegalArgumentException("Chave Pix inválida ou não informada.").also {
                logger.error("Chave Pix inválida ou não informada.")
            }
        }

    }

}
