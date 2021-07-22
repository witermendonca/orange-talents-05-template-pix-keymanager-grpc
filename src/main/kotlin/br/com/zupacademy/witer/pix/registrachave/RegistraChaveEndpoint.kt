package br.com.zupacademy.witer.pix.registrachave

import br.com.zupacademy.witer.KeyManagerRegistraGRPCServiceGrpc
import br.com.zupacademy.witer.RegistraChavePixRequest
import br.com.zupacademy.witer.RegistraChavePixResponse
import br.com.zupacademy.witer.compartilhado.handlers.ErrorHandler
import br.com.zupacademy.witer.pix.paraNovaChavePix
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class RegistraChaveEndpoint(@Inject private val registraChaveService: RegistraChaveService) :
    KeyManagerRegistraGRPCServiceGrpc.KeyManagerRegistraGRPCServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun registra(
        request: RegistraChavePixRequest?, responseObserver: StreamObserver<RegistraChavePixResponse>?,
    ) {

        val novaChave = request?.paraNovaChavePix()
        val chaveCriada = registraChaveService.registra(novaChave)

        logger.info("Chave Pix Cadastrada. ClienteId: ${chaveCriada.clienteId}, ChavePixId: ${chaveCriada.id}")

        responseObserver?.onNext(RegistraChavePixResponse.newBuilder()
            .setClienteId(chaveCriada.clienteId.toString())
            .setChavePixId(chaveCriada.id.toString())
            .build())
        responseObserver?.onCompleted()
    }
}