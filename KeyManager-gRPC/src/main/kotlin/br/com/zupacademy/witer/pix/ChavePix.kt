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
    @Column(name = "cliente_id", nullable = false)
    val clienteId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_chave",nullable = false)
    val tipoChave: TipoChave,

    @field:NotBlank
    @field:Size(max = 77)
    @Column(unique = true, length = 77, nullable = false)
    val chave: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta",nullable = false)
    val tipoConta: TipoConta,

    @field:Valid
    @field:NotNull
    @Embedded
    val conta: ContaAssociada,

    ) {

    @Id
    @GeneratedValue
    val id: UUID? = null

    @Column(name = "criada_em",nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

}
