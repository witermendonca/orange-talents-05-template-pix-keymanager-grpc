package br.com.zupacademy.witer.pix.removechave

import br.com.zupacademy.witer.KeyManagerRemoveGRPCServiceGrpc
import br.com.zupacademy.witer.RemoveChavePixRequest
import br.com.zupacademy.witer.pix.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class RemoveChavePixEndpointTest(
    val chavePixRepository: ChavePixRepository,
    val grpcClient: KeyManagerRemoveGRPCServiceGrpc.KeyManagerRemoveGRPCServiceBlockingStub,
) {

    lateinit var CHAVE_PIX: ChavePix

    //Salva chavePix no banco de dados(ChavePix já existente)
    @BeforeEach
    fun setUp() {
        CHAVE_PIX = chavePixRepository.save(ChavePix(
            clienteId = UUID.randomUUID(),
            tipoChave = TipoChave.CELULAR,
            chave = "+5585988714077",
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "ITAÚ UNIBANCO S.A.",
                nomeDoTitular = "Witer Mendonça",
                cpfDoTitular = "85964254039",
                agencia = "0001",
                numeroDaConta = "123499"
            )
        ))
    }

    //Limpa banco de dados no final dos testes.
    @AfterEach
    fun cleanUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve remover chave pix existente`() {

        //cenario(chavePix já existente no banco de dados)

        //ação(passar os dados para remover chave pix pelo gRPClient.)
        val response = grpcClient.remove(RemoveChavePixRequest.newBuilder()
            .setClienteId(CHAVE_PIX.clienteId.toString())
            .setChavePixId(CHAVE_PIX.id.toString())
            .build()
        )

        //validação(Validação da resposta com o idChavePix e idCliente da chave removida.)
        assertEquals(CHAVE_PIX.clienteId.toString(), response.clienteId)
        assertEquals(CHAVE_PIX.id.toString(), response.chavePixId)
    }

    @Test
    fun `nao deve remover chave pix inexistente`() {

        //cenario(Gera uma chavePix aleatoria inexistente no sitema.)
        val chavePixInexistente = UUID.randomUUID()

        //ação(passar os dados para remover chave pix pelo gRPClient com chave inexistente.(Erro esperado))
        val throws = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setChavePixId(chavePixInexistente.toString())
                .setClienteId(CHAVE_PIX.clienteId.toString())
                .build()
            )
        }

        //validação(Validação da exception lançada pelo status code e description.)
        with(throws) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada.", status.description)
        }

    }

    @Test
    fun `nao deve remover chave pix existente porem nao pertencente ao cliente`() {

        //cenario(Gera um clienteId aleatorio)
        val outroClienteId = UUID.randomUUID()

        //ação(passar os dados para remover chave pix pelo gRPClient com clienteId de outra conta.(Erro esperado))
        val throws = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setChavePixId(CHAVE_PIX.id.toString())
                .setClienteId(outroClienteId.toString())
                .build()
            )
        }

        //validação(Validação da exception lançada pelo status code e description.)
        with(throws) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada.", status.description)
        }
    }

    //serviço gRPC
    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRemoveGRPCServiceGrpc.KeyManagerRemoveGRPCServiceBlockingStub? {
            return KeyManagerRemoveGRPCServiceGrpc.newBlockingStub(channel)
        }
    }

}