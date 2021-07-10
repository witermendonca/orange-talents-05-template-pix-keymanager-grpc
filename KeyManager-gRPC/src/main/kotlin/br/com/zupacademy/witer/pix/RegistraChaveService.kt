package br.com.zupacademy.witer.pix

import br.com.zupacademy.witer.exceptions.ChavePixExistenteException
import br.com.zupacademy.witer.externo.ItauClient
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
class RegistraChaveService(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val itauClient: ItauClient,
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    fun registra(@Valid novaChavePix: NovaChavePix?): ChavePix {

        //verifica se chave já existe no sistema
        if (chavePixRepository.existsByChave(novaChavePix?.chave)) {
            logger.error("Chave Pix '${novaChavePix?.chave}' já cadastrada no sistema.")
            throw ChavePixExistenteException("Chave Pix '${novaChavePix?.chave}' já cadastrada no sistema.")
        }

        //busca dados da conta no ERP do ITAU
        val response = itauClient.buscaContaClienteIdETipo(novaChavePix?.clienteId!!, novaChavePix?.tipoContaPix!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itau")
            .apply { logger.error("Cliente não encontrado no Itau") }


        //Persiste no banco de dados chavePix
        val novaChaveCadastrada = chavePixRepository.save(novaChavePix.paraChavePix(conta))

        return novaChaveCadastrada
    }
}