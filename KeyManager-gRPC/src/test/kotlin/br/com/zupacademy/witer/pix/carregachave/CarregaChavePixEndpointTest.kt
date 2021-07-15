package br.com.zupacademy.witer.pix.carregachave

import br.com.zupacademy.witer.CarregaChavePixRequest
import br.com.zupacademy.witer.KeyManagerCarregaGRPCServiceGrpc
import br.com.zupacademy.witer.externo.bcb.*
import br.com.zupacademy.witer.pix.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class CarregaChavePixEndpointTest(
    val chavePixRepository: ChavePixRepository,
    val grpcClient: KeyManagerCarregaGRPCServiceGrpc.KeyManagerCarregaGRPCServiceBlockingStub,
) {

    @Inject
    lateinit var bcbClient: BancoCentralClient

    //Gera UUID para Cliente
    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    //Salva chavePix no banco em memoria antes dos testes.
    @BeforeEach
    fun setup() {
        chavePixRepository.save(chavePix(tipoChave = TipoChave.EMAIL, chave = "y.matheus@zup.com.br", clienteId = CLIENTE_ID))
        chavePixRepository.save(chavePix(tipoChave = TipoChave.CPF, chave = "63657520325", clienteId = UUID.randomUUID()))
        chavePixRepository.save(chavePix(tipoChave = TipoChave.ALEATORIA, chave = "randomkey-3", clienteId = CLIENTE_ID))
        chavePixRepository.save(chavePix(tipoChave = TipoChave.CELULAR, chave = "+551155554321", clienteId = CLIENTE_ID))
    }


    //Limpa banco em memoria depois dos testes.
    @AfterEach
    fun cleanUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve carregar chavePix por pixId e clienteId`() {

        //cenario(Busca chavePix no banco(chave do tipoChave CELULAR).
        val chavePixExistente = chavePixRepository.findByChave("+551155554321").get()

        //ação(passar os dados para carregar chave pix pelo gRPClient.(Carregar pelo pixId e clienteId.))
        val response = grpcClient.carrega(CarregaChavePixRequest.newBuilder()
            .setPixEClienteId(CarregaChavePixRequest.FiltroPorPixEClienteId.newBuilder()
                .setPixId(chavePixExistente.id.toString())
                .setClienteId(chavePixExistente.clienteId.toString())
                .build()
            ).build())

        // validação(validar resposta quando carregar informações da chavePix for realizado com sucesso.)
        with(response) {
            assertEquals(chavePixExistente.id.toString(), this.pixId)
            assertEquals(chavePixExistente.clienteId.toString(), this.clienteId)
            assertEquals(chavePixExistente.tipoChave.name, this.chavePix.tipo.name)
            assertEquals(chavePixExistente.chave, this.chavePix.chave)
            assertEquals(chavePixExistente.conta.instituicao, this.chavePix.conta.instituicao)
            assertEquals(chavePixExistente.conta.nomeDoTitular, this.chavePix.conta.nomeDoTitular)
            assertEquals(chavePixExistente.conta.cpfDoTitular, this.chavePix.conta.cpfDoTitular)
            assertEquals(chavePixExistente.conta.agencia, this.chavePix.conta.agencia)
            assertEquals(chavePixExistente.conta.numeroDaConta, this.chavePix.conta.numeroDaConta)
        }
    }

    @Test
    fun `deve carregar chavePix por valor da chavePix quando registro existir localmente`() {

        //cenario(Busca chavePix no banco(chave do tipoChave EMAIL).
        val chavePixExistente = chavePixRepository.findByChave("y.matheus@zup.com.br").get()

        //ação(passar os dados para carregar chave pix pelo gRPClient.(Carregar pela chavePix))
        val response = grpcClient.carrega(CarregaChavePixRequest.newBuilder()
            .setChave("y.matheus@zup.com.br")
            .build())

        // validação(validar resposta quando carregar informações da chavePix for realizado com sucesso.)
        with(response) {
            assertEquals(chavePixExistente.id.toString(), this.pixId)
            assertEquals(chavePixExistente.clienteId.toString(), this.clienteId)
            assertEquals(chavePixExistente.tipoChave.name, this.chavePix.tipo.name)
            assertEquals(chavePixExistente.chave, this.chavePix.chave)
            assertEquals(chavePixExistente.conta.instituicao, this.chavePix.conta.instituicao)
            assertEquals(chavePixExistente.conta.nomeDoTitular, this.chavePix.conta.nomeDoTitular)
            assertEquals(chavePixExistente.conta.cpfDoTitular, this.chavePix.conta.cpfDoTitular)
            assertEquals(chavePixExistente.conta.agencia, this.chavePix.conta.agencia)
            assertEquals(chavePixExistente.conta.numeroDaConta, this.chavePix.conta.numeroDaConta)
        }
    }

    @Test
    fun `deve carregar chavePix por valor da chavePix quando registro nao existir localmente mas existir no BCB`() {

        //cenario(Busca chavePix no sitema externo BCB.(Busca pela chavePix existente apenas no BCB.))
        val bcbResponse = pixKeyDetailsResponse()
        `when`(bcbClient.findByPixKeyBcb(key = "user.from.another.bank@santander.com.br"))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))

        //ação(passar os dados para carregar chave pix pelo gRPClient.(Carregar pela chavePix))
        val response = grpcClient.carrega(CarregaChavePixRequest.newBuilder()
            .setChave("user.from.another.bank@santander.com.br")
            .build())

        // validação(validar resposta quando carregar informações da chavePix for realizado com sucesso.)
        with(response) {
            assertEquals("", this.pixId)
            assertEquals("", this.clienteId)
            assertEquals(bcbResponse.keyType.name, this.chavePix.tipo.name)
            assertEquals(bcbResponse.key, this.chavePix.chave)
            assertEquals(bcbResponse.owner.name, this.chavePix.conta.nomeDoTitular)
            assertEquals(bcbResponse.owner.taxIdNumber, this.chavePix.conta.cpfDoTitular)
            assertEquals(bcbResponse.bankAccount.branch, this.chavePix.conta.agencia)
            assertEquals(bcbResponse.bankAccount.accountNumber, this.chavePix.conta.numeroDaConta)
        }
    }

    @Test
    fun `nao deve carregar chavePix por pixId e clienteId quando filtro invalido`() {

        //cenario(dados inválidos passados pelo gRPCClient.(Erro esperado.))

        //ação(passar os dados para carregar chave pix pelo gRPClient.(Dados inválidos))
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.carrega(CarregaChavePixRequest.newBuilder()
                .setPixEClienteId(CarregaChavePixRequest.FiltroPorPixEClienteId.newBuilder()
                    .setPixId("")
                    .setClienteId("")
                    .build()
                ).build())
        }

        //validação(Validar o Status code do erro.)
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `nao deve carregar chavePix por pixId e clienteId quando registro nao existir`() {

        //cenario(dados inexistentes passados pelo gRPCClient.(Erro esperado.))

        //ação(passar os dados para carregar chave pix pelo gRPClient.(Dados inexistentes))
        val pixIdNaoExistente = UUID.randomUUID().toString()
        val clienteIdNaoExistente = UUID.randomUUID().toString()
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.carrega(CarregaChavePixRequest.newBuilder()
                .setPixEClienteId(CarregaChavePixRequest.FiltroPorPixEClienteId.newBuilder()
                    .setPixId(pixIdNaoExistente)
                    .setClienteId(clienteIdNaoExistente)
                    .build()
                ).build())
        }

        //validação(Validar o Status code e description do erro.)
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix Não Encontrada.", status.description)
        }
    }

    @Test
    fun `nao deve carregar chavePix por valor da chavePix quando registro nao existir localmente nem no BCB`() {

        //cenario(Busca chavePix no sitema externo BCB.(Busca pela chavePix inexistente no BCB.))
        `when`(bcbClient.findByPixKeyBcb(key = "not.existing.user@santander.com.br"))
            .thenReturn(HttpResponse.notFound())

        //ação(passar os dados para carregar chave pix pelo gRPClient.(Dados inexistentes))
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.carrega(CarregaChavePixRequest.newBuilder()
                .setChave("not.existing.user@santander.com.br")
                .build())
        }

        //validação(Validar o Status code e description do erro.)
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix Não Encontrada.", status.description)
        }
    }

    @Test
    fun `nao deve carregar chavePix por valor da chavePix quando filtro invalido`() {

        //cenario(dados inválidos passados pelo gRPCClient.(Erro esperado.))

        //ação(passar os dados para carregar chave pix pelo gRPClient.(Dados inválidos))
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.carrega(CarregaChavePixRequest.newBuilder().setChave("").build())
        }

        //validação(Validar o Status code e description do erro.)
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("chave: não deve estar em branco", status.description)
        }
    }

    @Test
    fun `nao deve carregar chavePix quando filtro invalido`() {

        //cenario(dados inválidos passados pelo gRPCClient.(Erro esperado.))

        //ação(passar os dados para carregar chave pix pelo gRPClient.(Dados inválidos))
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.carrega(CarregaChavePixRequest.newBuilder().build())
        }

        //validação(Validar o Status code e description do erro.)
        with(thrown) {
            Assertions.assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            Assertions.assertEquals("Chave Pix inválida ou não informada.", status.description)
        }
    }

    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient? {
        return mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerCarregaGRPCServiceGrpc.KeyManagerCarregaGRPCServiceBlockingStub? {
            return KeyManagerCarregaGRPCServiceGrpc.newBlockingStub(channel)
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

    private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = PixKeyType.EMAIL,
            key = "user.from.another.bank@santander.com.br",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = "90400888",
            branch = "9871",
            accountNumber = "987654",
            accountType = BankAccount.AccountType.SVGS
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Another User",
            taxIdNumber = "12345678901"
        )
    }
}

//{
//    "clienteId": "",
//    "pixId": "",
//    "chavePix": {
//    "tipo": "CPF",
//    "chave": "33059192057",
//    "conta": {
//    "tipo": "CONTA_CORRENTE",
//    "instituicao": "ITAÚ UNIBANCO S.A.",
//    "nomeDoTitular": "Steve Jobs",
//    "cpfDoTitular": "33059192057",
//    "agencia": "0001",
//    "numeroDaConta": "123456"
//},
