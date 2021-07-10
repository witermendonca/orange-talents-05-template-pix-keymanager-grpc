package br.com.zupacademy.witer.pix

import br.com.zupacademy.witer.compartilhado.validator.ValidPixKey
import br.com.zupacademy.witer.compartilhado.validator.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@ValidPixKey
data class NovaChavePix(
    @field: NotBlank @field: ValidUUID val clienteId: String?,
    @field: NotNull val tipoChavePix: TipoChave?,
    @field: Size(max = 77) val chave: String?,
    @field: NotNull val tipoContaPix: TipoConta?,
) {

    fun paraChavePix(conta: ContaAssociada): ChavePix {
        return ChavePix(
            clienteId = UUID.fromString(this.clienteId),
            tipoChave = TipoChave.valueOf(this.tipoChavePix!!.name),
            chave = if (this.tipoChavePix == TipoChave.ALEATORIA) UUID.randomUUID().toString() else this.chave!!,
            tipoConta = TipoConta.valueOf(this.tipoContaPix!!.name),
            conta = conta
        )
    }
}
