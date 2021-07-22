package br.com.zupacademy.witer.pix

import org.hibernate.validator.constraints.br.CPF
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Embeddable //Cada uma das propriedades persistentes do objeto incorporado é mapeado para a tabela de dados da entidade
class ContaAssociada(

    @field:NotBlank
    val instituicao: String,

    @field:NotBlank
    @Column(name = "conta_titular_nome", nullable = false)
    val nomeDoTitular: String,

    @field:NotBlank
    @field:Size(max = 11)
    @field:CPF
    @Column(name = "conta_titular_cpf", length = 11, nullable = false)
    val cpfDoTitular: String,

    @field:NotBlank
    @field:Size(max = 4)
    @Column(name = "conta_agencia", length = 4, nullable = false)
    val agencia: String,

    @field:NotBlank
    @field:Size(max = 6)
    @Column(name = "conta_numero", length = 6, nullable = false)
    val numeroDaConta: String,
) {

    companion object {
        public val ITAU_UNIBANCO_ISPB: String = "60701190"
    }
}