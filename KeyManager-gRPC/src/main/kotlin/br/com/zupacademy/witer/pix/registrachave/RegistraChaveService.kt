package br.com.zupacademy.witer.pix.registrachave

import br.com.zupacademy.witer.exceptions.ChavePixExistenteException
import br.com.zupacademy.witer.externo.ItauClient
import br.com.zupacademy.witer.externo.bcb.BancoCentralClient
import br.com.zupacademy.witer.externo.bcb.CreatePixKeyBcbRequest
import br.com.zupacademy.witer.pix.ChavePix
import br.com.zupacademy.witer.pix.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
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
    @Inject val bancoCentralClient: BancoCentralClient,
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
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Conta não encontrada no Sistema Itaú")
            .also { logger.error("Conta não encontrada no Sistema Itaú") }


        //Persiste no banco de dados chavePix
        val novaChaveCadastrada = chavePixRepository.save(novaChavePix.paraChavePix(conta))

        try {
            //Tenta registra novaChavePix no Banco Central.
            val bcbResponse = bancoCentralClient.createPixKeyBcb(CreatePixKeyBcbRequest.of(novaChaveCadastrada).also {
                logger.info("Registrando chave Pix no Banco Central do Brasil (BCB): $it")
            })

            //para validar no teste tenho que capturar o erro aqui.
            if (bcbResponse.status != HttpStatus.CREATED)
                throw IllegalStateException("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)")

            //Atualiza chave do dominio com chave gerada pelo BCB caso tipoChave for aletoria.
            novaChaveCadastrada.atualiza(bcbResponse.body()!!.key)

        } catch (e: HttpClientResponseException) {
            if (e.status == HttpStatus.UNPROCESSABLE_ENTITY) { //status code 422
                throw IllegalStateException("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)")
            }
        }

        return novaChaveCadastrada
    }
}