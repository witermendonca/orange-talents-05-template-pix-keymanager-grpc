package br.com.zupacademy.witer.pix.listachaves

import br.com.zupacademy.witer.*
import br.com.zupacademy.witer.compartilhado.handlers.ErrorHandler
import br.com.zupacademy.witer.exceptions.ClienteIdNaoEncontradoException
import br.com.zupacademy.witer.pix.ChavePixRepository
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class ListaChavePixEndpoint(
    @Inject val chavePixRepository: ChavePixRepository,
) : KeyManagerListaGRPCServiceGrpc.KeyManagerListaGRPCServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun lista(request: ListaChavePixRequest, responseObserver: StreamObserver<ListaChavePixResponse>) {

        if (request.clienteId.isNullOrBlank()) {
            logger.error("Cliente Id não pode ser nulo ou vazio.")
            throw IllegalArgumentException("Cliente Id não pode ser nulo ou vazio.")
        }

        val listaDeChavesPix = chavePixRepository.findAllByClienteId(UUID.fromString(request.clienteId)).map {
            ListaChavePixResponse.ChavePix.newBuilder()
                .setChavePixId(it.id.toString())
                .setTipoChave(TipoChave.valueOf(it.tipoChave.name)) // 1
                .setChave(it.chave)
                .setTipoConta(TipoConta.valueOf(it.tipoConta.name)) // 1
                .setCriadaEm(it.criadaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build()
        }

        responseObserver.onNext(ListaChavePixResponse.newBuilder()
            .setClienteId(request.clienteId)
            .addAllChavesPix(listaDeChavesPix)
            .build()
        )
        responseObserver.onCompleted()
    }
}