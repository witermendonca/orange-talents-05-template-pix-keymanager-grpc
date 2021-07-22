package br.com.zupacademy.witer.externo.bcb

import br.com.zupacademy.witer.pix.ContaAssociada

data class DeletePixKeyBcbRequest(
    val key: String,
    val participant: String = ContaAssociada.ITAU_UNIBANCO_ISPB,
) {

}

//DeletePixKeyRequest{
//    key*	string
//    participant*	string
//}