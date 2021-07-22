package br.com.zupacademy.witer.externo.bcb

import br.com.zupacademy.witer.pix.ContaAssociada
import br.com.zupacademy.witer.pix.TipoConta
import br.com.zupacademy.witer.pix.carregachave.ChavePixInfo
import br.com.zupacademy.witer.pix.carregachave.Instituicoes
import java.time.LocalDateTime

data class PixKeyDetailsResponse(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime,
) {
    fun toModel(): ChavePixInfo {
        return ChavePixInfo(
            tipochave = keyType.domainType!!,
            chave = this.key,
            tipoConta = when (this.bankAccount.accountType) {
                BankAccount.AccountType.CACC -> TipoConta.CONTA_CORRENTE
                BankAccount.AccountType.SVGS -> TipoConta.CONTA_POUPANCA
            },
            conta = ContaAssociada(
                instituicao = Instituicoes.nome(bankAccount.participant),
                nomeDoTitular = owner.name,
                cpfDoTitular = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numeroDaConta = bankAccount.accountNumber
            )
        )
    }
}

//PixKeyDetailsResponse{
//    keyType	string
//              Enum:
//                  [ CPF, CNPJ, PHONE, EMAIL, RANDOM ]
//    key	   string
//    bankAccount	BankAccountResponse{
//                       participant	string
//                       branch	        string
//                       accountNumber	string
//                       accountType	string
//                                      Enum:
//                                          [ CACC, SVGS ]
//                  }
//    owner	OwnerResponse{
//                      type	string
//                              Enum:
//                                 [ NATURAL_PERSON, LEGAL_PERSON ]
//                      name	string
//                      taxIdNumber	string
//                 }
//    createdAt	string($date-time)
//}