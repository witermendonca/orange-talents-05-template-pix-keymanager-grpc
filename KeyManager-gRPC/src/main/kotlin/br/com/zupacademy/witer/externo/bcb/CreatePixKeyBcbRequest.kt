package br.com.zupacademy.witer.externo.bcb

import br.com.zupacademy.witer.pix.ChavePix
import br.com.zupacademy.witer.pix.ContaAssociada

data class CreatePixKeyBcbRequest(
    //TipoChave
    val keyType: PixKeyType,
    //chave
    val key: String,
    //conta
    val bankAccount: BankAccount,
    //cliente
    val owner: Owner,
) {

    companion object {

        fun of(chave: ChavePix): CreatePixKeyBcbRequest {
            return CreatePixKeyBcbRequest(
                keyType = PixKeyType.by(chave.tipoChave),
                key = chave.chave,
                bankAccount = BankAccount(
                    participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                    branch = chave.conta.agencia,
                    accountNumber = chave.conta.numeroDaConta,
                    accountType = BankAccount.AccountType.by(chave.tipoConta),
                ),
                owner = Owner(
                    type = Owner.OwnerType.NATURAL_PERSON,
                    name = chave.conta.nomeDoTitular,
                    taxIdNumber = chave.conta.cpfDoTitular
                )
            )
        }
    }
}


//CreatePixKeyRequest{
//    keyType*	string
//                  example: CPF
//                  Tipo de chave. Novos tipos podem surgir
//
//                  Enum:
//                  Array [ 5 ]
//    key	string
//                  maxLength: 77
//                  minLength: 0
//                  example: 33059192057
//                  Chave de endereçamento
//
//    bankAccount*	BankAccountRequest{...}
//    owner*	OwnerRequest{...}
//}