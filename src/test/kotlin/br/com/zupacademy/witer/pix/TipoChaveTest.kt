package br.com.zupacademy.witer.pix

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TipoChaveTest {

    @Nested
    inner class CPF {

        @Test
        fun `deve ser valido quando cpf for um numero valido`() {
            with(TipoChave.CPF) {
                assertTrue(valida("704.844.250-64"))
            }
        }

        @Test
        fun `nao deve ser valido quando cpf for um numero invalido`() {
            with(TipoChave.CPF) {
                assertFalse(valida("000.000.000-09"))
            }
        }

        @Test
        fun `nao deve ser valido quando cpf nao for informado`() {
            with(TipoChave.CPF) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }

        @Test
        fun `nao deve ser valido quando cpf possuir letras`() {
            with(TipoChave.CPF) {
                assertFalse(valida("704.844.250-6a"))
            }
        }
    }

    @Nested
    inner class EMAIL {

        @Test
        fun `deve ser valido quando email for endereco valido`() {
            with(TipoChave.EMAIL) {
                assertTrue(valida("witer.mendonca@zup.com.br"))
            }
        }

        @Test
        fun `nao deve ser valido quando email estiver em um formato invalido`() {
            with(TipoChave.EMAIL) {
                assertFalse(valida("witer.mendoncazup.com.br"))
                assertFalse(valida("witer.mendonca@zup.com."))
            }
        }

        @Test
        fun `nao deve ser valido quando email nao for informado`() {
            with(TipoChave.EMAIL) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }

        }
    }

    @Nested
    inner class CELULAR {

        @Test
        fun `deve ser valido quando celular for um numero valido`() {
            with(TipoChave.CELULAR) {
                assertTrue(valida("+5585988714077"))
            }
        }

        @Test
        fun `nao deve ser valido quando celular for um numero invalido`() {
            with(TipoChave.CELULAR) {
                assertFalse(valida("5585988714077"))
                assertFalse(valida("+5585988714a77"))
            }
        }

        @Test
        fun `nao deve ser valido quando celular nao for informado`() {
            with(TipoChave.CELULAR) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }
    }

    @Nested
    inner class ALEATORIA {

        @Test
        fun `deve ser valido quando chave aleatoria for nula ou vazia`() {
            with(TipoChave.ALEATORIA) {
                assertTrue(valida(null))
                assertTrue(valida(""))
            }
        }

        @Test
        fun `nao deve ser valido quando chave aleatoria possuir um valor`() {
            with(TipoChave.ALEATORIA) {
                assertFalse(valida("qualquer valor CPF, Email, Celular."))
            }
        }
    }
}