package br.com.zupacademy.witer.pix

import br.com.zupacademy.witer.KeyManagerRegistraGRPCServiceGrpc
import br.com.zupacademy.witer.RegistraChavePixRequest
import br.com.zupacademy.witer.RegistraChavePixResponse
import br.com.zupacademy.witer.externo.ItauClient
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import java.lang.RuntimeException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegistraChaveEndpoint(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val itauClient: ItauClient,
) : KeyManagerRegistraGRPCServiceGrpc.KeyManagerRegistraGRPCServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun registra(
        request: RegistraChavePixRequest?, responseObserver: StreamObserver<RegistraChavePixResponse>?) {

        //verifica se chave já existe no sistema
        if (chavePixRepository.existsByChave(request?.chave))
            responseObserver?.onError(Status.ALREADY_EXISTS
                .withDescription("Chave Pix '${request?.chave}' existente")
                .asRuntimeException()
            ).also(throw RuntimeException("Chave Pix '${request?.chave}' existente"))

        //busca dados da conta no ERP do ITAU
        val response = itauClient.buscaContaClienteIdETipo(request?.clienteId!!, request?.tipoConta!!.name)
        val conta = response.body() ?: throw IllegalStateException("Cliente não encontrado no Itau")

        //Persiste no banco de dados chavePix
        val novaChave = chavePixRepository.save(request.paraNovaChavePix().paraChavePix(conta.toModel()))

        logger.info("Chave Pix Cadastrada. ClienteId: ${novaChave.clienteId}, ChavePixId: ${novaChave.id}")

        responseObserver?.onNext(RegistraChavePixResponse.newBuilder()
            .setClienteId(novaChave.clienteId.toString())
            .setChavePixId(novaChave.id.toString())
            .build())
        responseObserver?.onCompleted()
    }
}