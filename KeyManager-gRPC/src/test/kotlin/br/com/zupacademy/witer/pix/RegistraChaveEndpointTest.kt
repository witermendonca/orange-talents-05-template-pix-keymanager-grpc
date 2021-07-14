package br.com.zupacademy.witer.pix

import br.com.zupacademy.witer.KeyManagerRegistraGRPCServiceGrpc
import br.com.zupacademy.witer.RegistraChavePixRequest
import br.com.zupacademy.witer.TipoChave
import br.com.zupacademy.witer.TipoConta
import br.com.zupacademy.witer.externo.DadosDaContaResponse
import br.com.zupacademy.witer.externo.InstituicaoResponse
import br.com.zupacademy.witer.externo.ItauClient
import br.com.zupacademy.witer.externo.TitularResponse
import br.com.zupacademy.witer.externo.bcb.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerRegistraGRPCServiceGrpc.KeyManagerRegistraGRPCServiceBlockingStub,
) {

    @Inject
    lateinit var itauClient: ItauClient;

    @Inject
    lateinit var bcbClient: BancoCentralClient


    //Gera UUID para Cliente
    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }


    //Limpa o banco para testes
    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve registrar nova chave pix`() {

        //cenario (fazer a busca da conta no sitema externo itau e ela devolver uma canta válida.)
        `when`(itauClient.buscaContaClienteIdETipo(clienteId = CLIENTE_ID.toString(),
            tipo = TipoConta.CONTA_CORRENTE.toString())).thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        //cenario (enviar dados chavePix para cadastro no BCB e retorno for created.)
        `when`(bcbClient.createPixKeyBcb(createPixKeyBcbRequest()))
            .thenReturn(HttpResponse.created(createPixKeyBcbResponse()))


        //ação (passar os dados para cadastro de nova chave pix pelo gRPClient.)
        val response = grpcClient.registra(RegistraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setTipoChave(TipoChave.EMAIL)
            .setChave("y.matheus@zup.com.br")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build())

        //validação (validar resposta da quando registro for realizado com sucesso.)
        with(response) {
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertNotNull(chavePixId)
        }

    }

    @Test
    fun `nao deve cadastrar chave pix quando ja estiver cadastrada no sitema`() {

        //cenario (chave pix já ser cadastrada no sitema.)
        repository.save(chavePix())

        //ação(passar os dados para cadastro de nova chave pix pelo gRPClient. Erro esperado.)
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoChave(TipoChave.CELULAR)
                .setChave("+5585988714077")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build())
        }

        //validação(Validar o Status code e description do erro.)
        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix '+5585988714077' já cadastrada no sistema.", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar chave pix quando nao for encontrada conta pelo id no sitema externo itau`() {

        //cenario(fazer a busca da conta no sitema externo itau e ela devolver not found(conta não encontrada).)
        `when`(itauClient.buscaContaClienteIdETipo(clienteId = CLIENTE_ID.toString(),
            tipo = TipoConta.CONTA_POUPANCA.toString())).thenReturn(HttpResponse.notFound())

        //ação(passar os dados para cadastro de nova chave pix pelo gRPClient. Erro esperado.)
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoChave(TipoChave.ALEATORIA)
                .setTipoConta(TipoConta.CONTA_POUPANCA)
                .build())
        }

        //validação(Validar o Status code e description do erro.)
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Conta não encontrada no Sistema Itaú", status.description)
        }

    }

    @Test
    fun `nao deve cadastrar chave pix quando dados forem invalidos`() {

        //cenario(nenhum dado válido informado.)

        //ação(passar os dados invalidos para cadastro de nova chave pix pelo gRPClient. Erro esperado.)
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder().build())
        }

        //validação(Validar o Status code do erro.)
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }

    }

    @Test
    fun `nao deve cadastrar chave pix quando retorno bcb for diferente que httpStatus created`() {

        //cenario (fazer a busca da conta no sitema externo itau e ela devolver uma canta válida.)
        `when`(itauClient.buscaContaClienteIdETipo(clienteId = CLIENTE_ID.toString(),
            tipo = TipoConta.CONTA_CORRENTE.toString())).thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        //cenario (enviar dados chavePix para cadastro BCB e retorno for badRequest. Erro esperado .)
        `when`(bcbClient.createPixKeyBcb(createPixKeyBcbRequest()))
            .thenReturn(HttpResponse.status(HttpStatus.UNPROCESSABLE_ENTITY))

        //ação(passar os dados para cadastro de nova chave pix pelo gRPClient. Erro esperado.)
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoChave(TipoChave.EMAIL)
                .setChave("y.matheus@zup.com.br")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build())
        }

        //validação(Validar o Status code e description do erro.)
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)", status.description)
        }
    }

    private fun chavePix() = ChavePix(
        clienteId = CLIENTE_ID,
        tipoChave = br.com.zupacademy.witer.pix.TipoChave.CELULAR,
        chave = "+5585988714077",
        tipoConta = br.com.zupacademy.witer.pix.TipoConta.CONTA_CORRENTE,
        conta = ContaAssociada(
            instituicao = "ITAÚ UNIBANCO S.A.",
            nomeDoTitular = "Witer Mendonça",
            cpfDoTitular = "85964254039",
            agencia = "0001",
            numeroDaConta = "123499"
        )
    )

    private fun dadosDaContaResponse() = DadosDaContaResponse(
        tipo = "CONTA_CORRENTE",
        instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A.", "60701190"),
        agencia = "0001",
        numero = "123455",
        titular = TitularResponse("Yuri Matheus", "86135457004")
    )

    private fun createPixKeyBcbRequest() = CreatePixKeyBcbRequest(
        keyType = PixKeyType.EMAIL,
        key = "y.matheus@zup.com.br",
        bankAccount = bankAccount(),
        owner = owner()
    )

    private fun createPixKeyBcbResponse() = CreatePixKeyBcbResponse(
        keyType = PixKeyType.EMAIL,
        key = "y.matheus@zup.com.br",
        bankAccount = bankAccount(),
        owner = owner(),
        createdAt = LocalDateTime.now()
    )

    private fun owner() = Owner(
        type = Owner.OwnerType.NATURAL_PERSON,
        name = "Yuri Matheus",
        taxIdNumber = "86135457004"
    )

    private fun bankAccount() = BankAccount(
        participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
        branch = "0001",
        accountNumber = "123455",
        accountType = BankAccount.AccountType.CACC,
    )

    //serviço gRPC
    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRegistraGRPCServiceGrpc.KeyManagerRegistraGRPCServiceBlockingStub? {
            return KeyManagerRegistraGRPCServiceGrpc.newBlockingStub(channel)
        }
    }

    //mock servidor itau
    @MockBean(ItauClient::class)
    fun itauClient(): ItauClient? {
        return mock(ItauClient::class.java)
    }

    //mock servidor bcb
    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient {
        return mock(BancoCentralClient::class.java)
    }
}

//{
//    "tipo": "CONTA_CORRENTE",
//    "instituicao": {
//    "nome": "ITAÚ UNIBANCO S.A.",
//    "ispb": "60701190"
//},
//    "agencia": "0001",
//    "numero": "123455",
//    "titular": {
//    "id": "5260263c-a3c1-4727-ae32-3bdb2538841b",
//    "nome": "Yuri Matheus",
//    "cpf": "86135457004"
//}
//}
