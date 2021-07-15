package br.com.zupacademy.witer.pix

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class ChavePixTest{

    companion object {
        val TIPOS_DE_CHAVES_EXCETO_ALEATORIO = TipoChave.values().filterNot { it == TipoChave.ALEATORIA }
    }

    @Test
    fun deveChavePertencerAoCliente() {

        val clienteId = UUID.randomUUID()
        val outroClienteId = UUID.randomUUID()

        with (novaChave(tipoChave = TipoChave.ALEATORIA, clienteId = clienteId)) {
            assertTrue(this.pertenceAo(clienteId))
            assertFalse(this.pertenceAo(outroClienteId))
        }
    }

    @Test
    fun deveChaveSerDoTipoAleatoria() {
        with (novaChave(TipoChave.ALEATORIA)) {
            assertTrue(this.chaveAleatoria())
        }
    }

    @Test
    fun naoDeveChaveSerDoTipoAleatoria() {
        TIPOS_DE_CHAVES_EXCETO_ALEATORIO
            .forEach {
                assertFalse(novaChave(it).chaveAleatoria())
            }
    }

    @Test
    fun deveAtualizarChaveQuandoChaveForAleatoria() {
        with (novaChave(TipoChave.ALEATORIA)) {
            assertTrue(this.atualiza("nova-chave"))
            assertEquals("nova-chave", this.chave)
        }
    }

    @Test
    fun naoDeveAtualizarChaveQuandoChaveForDiferenteDeAleatoria() {

        val original = "<chave-aleatoria-qualquer>"

        TIPOS_DE_CHAVES_EXCETO_ALEATORIO
            .forEach {
                with (novaChave(tipoChave = it, chave = original)) {
                    assertFalse(this.atualiza("nova-chave"))
                    assertEquals(original, this.chave)
                }
            }
    }

    private fun novaChave(
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