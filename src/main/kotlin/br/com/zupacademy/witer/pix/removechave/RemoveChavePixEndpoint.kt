package br.com.zupacademy.witer.pix.removechave

import br.com.zupacademy.witer.KeyManagerRemoveGRPCServiceGrpc
import br.com.zupacademy.witer.RemoveChavePixRequest
import br.com.zupacademy.witer.RemoveChavePixResponse
import br.com.zupacademy.witer.compartilhado.handlers.ErrorHandler
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class RemoveChavePixEndpoint(
    @Inject val removeChavePixService: RemoveChavePixService,
) : KeyManagerRemoveGRPCServiceGrpc.KeyManagerRemoveGRPCServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun remove(request: RemoveChavePixRequest?, responseObserver: StreamObserver<RemoveChavePixResponse>?) {

        removeChavePixService.remove(request?.clienteId, request?.chavePixId)
        logger.info("Chave Pix Deletada. IdChavePix: ${request?.chavePixId}")
        responseObserver?.onNext(RemoveChavePixResponse.newBuilder()
            .setClienteId(request?.clienteId)
            .setChavePixId(request?.chavePixId)
            .build())
        responseObserver?.onCompleted()


    }

}