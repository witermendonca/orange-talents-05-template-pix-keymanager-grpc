package br.com.zupacademy.witer.externo.bcb

import java.time.LocalDateTime

data class CreatePixKeyBcbResponse(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime,
) {

}


//CreatePixKeyResponse{
//    keyType	string
//            Enum:
//                 [ CPF, CNPJ, PHONE, EMAIL, RANDOM ]
//    key	string
//    bankAccount	BankAccountResponse{
//                       participant	string
//                       branch	        string
//                       accountNumber	string
//                       accountType	string
//                                      Enum:
//                                          [ CACC, SVGS ]
//                   }
//    owner	OwnerResponse{
//                      type	string
//                              Enum:
//                                  [ NATURAL_PERSON, LEGAL_PERSON ]
//                      name	string
//                      taxIdNumber	string
//                 }
//    createdAt	string($date-time)
//}