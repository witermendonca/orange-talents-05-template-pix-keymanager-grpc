package br.com.zupacademy.witer.pix.listachaves

import br.com.zupacademy.witer.KeyManagerListaGRPCServiceGrpc
import br.com.zupacademy.witer.ListaChavePixRequest
import br.com.zupacademy.witer.pix.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class ListaChavePixEndpointTest(
    val chavePixRepository: ChavePixRepository,
    val grpcClient: KeyManagerListaGRPCServiceGrpc.KeyManagerListaGRPCServiceBlockingStub,
) {

    //Gera UUID para Cliente
    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    //Salva chavesPix no banco em memoria antes dos testes.
    @BeforeEach
    fun setup() {
        chavePixRepository.save(chavePix(tipoChave = TipoChave.EMAIL, chave = "y.matheus@zup.com.br", clienteId = CLIENTE_ID))
        chavePixRepository.save(chavePix(tipoChave = TipoChave.ALEATORIA, chave = "randomkey-2", clienteId = UUID.randomUUID()))
        chavePixRepository.save(chavePix(tipoChave = TipoChave.ALEATORIA, chave = "randomkey-3", clienteId = CLIENTE_ID))
        chavePixRepository.save(chavePix(tipoChave = TipoChave.CELULAR, chave = "+551155554321", clienteId = CLIENTE_ID))
        chavePixRepository.save(chavePix(tipoChave = TipoChave.CPF, chave = "86135457004", clienteId = CLIENTE_ID))
    }

    //Limpa banco em memoria depois dos testes.
    @AfterEach
    fun cleanUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve listar todas as chavesPix do cliente`() {

        //cenario(ClienteId com chaves Pix cadastradas.)
        val clienteId = CLIENTE_ID.toString()

        //ação(passar o clienteId para listar as chaves Pix pelo gRPClient.)
        val response = grpcClient.lista(ListaChavePixRequest.newBuilder()
            .setClienteId(clienteId)
            .build())

        //validação(Validar o número, tipo de chave e valor de chavesPix referente ao clienteId informado.)
        with(response.chavesPixList) {
            assertThat(this, hasSize(4))
            assertThat(
                this.map { Pair(it.tipoChave, it.chave) }.toList(),
                containsInAnyOrder(
                    Pair(br.com.zupacademy.witer.TipoChave.EMAIL, "y.matheus@zup.com.br"),
                    Pair(br.com.zupacademy.witer.TipoChave.ALEATORIA, "randomkey-3"),
                    Pair(br.com.zupacademy.witer.TipoChave.CELULAR, "+551155554321"),
                    Pair(br.com.zupacademy.witer.TipoChave.CPF, "86135457004")
                )
            )
        }
    }

    @Test
    fun `nao deve listar as chavesPix do cliente quando cliente nao possuir chavesPix`() {

        //cenario(ClienteId sem chaves Pix cadastradas.)
        val clienteSemChaves = UUID.randomUUID().toString()

        //ação(passar o clienteId sem chaves cadastradas para listar as chaves Pix pelo gRPClient.)
        val response = grpcClient.lista(ListaChavePixRequest.newBuilder()
            .setClienteId(clienteSemChaves)
            .build())

        //validação(Validar se o número de chavesPix da lista é 0.)
        assertEquals(0, response.chavesPixCount)
    }

    @Test
    fun `nao deve listar todas as chavesPix do cliente quando clienteId for invalido`() {

        //cenario(ClienteId vazio)
        val clienteIdInvalido = ""

        //ação(passar o clienteId vazio para listar as chaves Pix pelo gRPClient.(Erro esperado.))
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.lista(ListaChavePixRequest.newBuilder()
                .setClienteId(clienteIdInvalido)
                .build())
        }

        //validação(Validar o Status code e description do erro.)
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Cliente Id não pode ser nulo ou vazio.", status.description)
        }
    }


    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerListaGRPCServiceGrpc.KeyManagerListaGRPCServiceBlockingStub? {
            return KeyManagerListaGRPCServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chavePix(
        tipoChave: TipoChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipoChave = tipoChave,
            chave = chave,
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "ITAÚ UNIBANCO S.A.",
                nomeDoTitular = "Yuri Matheus",
                cpfDoTitular = "86135457004",
                agencia = "0001",
                numeroDaConta = "123455"
            )
        )
    }

}

