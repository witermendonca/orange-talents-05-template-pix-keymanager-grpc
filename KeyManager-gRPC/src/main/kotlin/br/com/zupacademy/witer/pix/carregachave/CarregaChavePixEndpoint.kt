package br.com.zupacademy.witer.pix.carregachave

import br.com.zupacademy.witer.CarregaChavePixRequest
import br.com.zupacademy.witer.CarregaChavePixResponse
import br.com.zupacademy.witer.KeyManagerCarregaGRPCServiceGrpc
import br.com.zupacademy.witer.compartilhado.handlers.ErrorHandler
import br.com.zupacademy.witer.externo.bcb.BancoCentralClient
import br.com.zupacademy.witer.pix.ChavePixRepository
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
@ErrorHandler
class CarregaChavePixEndpoint(
    @Inject val validator: Validator,
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val bancoCentralClient: BancoCentralClient,
    ) : KeyManagerCarregaGRPCServiceGrpc.KeyManagerCarregaGRPCServiceImplBase() {

    override fun carrega(request: CarregaChavePixRequest, responseObserver: StreamObserver<CarregaChavePixResponse>) {

        val filtro = request.toModel(validator)
        val chaveInfo = filtro.filtra(repository = chavePixRepository, bcbClient = bancoCentralClient)

        responseObserver.onNext(CarregaChavePixResponseConverter().convert(chaveInfo))
        responseObserver.onCompleted()
    }
}