package br.com.zupacademy.witer.pix

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChavePix(

    @field:NotNull
    @Column(name = "cliente_id", length = 16, nullable = false)
    val clienteId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_chave", nullable = false)
    val tipoChave: TipoChave,

    @field:NotBlank
    @field:Size(max = 77)
    @Column(unique = true, length = 77, nullable = false)
    var chave: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta", nullable = false)
    val tipoConta: TipoConta,

    @field:Valid
    @field:NotNull
    @Embedded
    val conta: ContaAssociada,

    ) {
    @Id
    @GeneratedValue
    @Column(length = 16)
    val id: UUID? = null

    @Column(name = "criada_em", nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

    fun chaveAleatoria(): Boolean {
        return tipoChave == TipoChave.ALEATORIA
    }

    fun atualiza(chave: String): Boolean {
        if (chaveAleatoria()) {
            this.chave = chave
            return true
        }
        return false
    }

    fun pertenceAo(clienteId: UUID) = this.clienteId.equals(clienteId)
}
