package br.com.zupacademy.witer.externo.bcb

import java.time.LocalDateTime

data class DeletePixKeyBcbResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime,
) {

}

//DeletePixKeyResponse{
//            key	string
//            participant	string
//            deletedAt	string($date-time)
//}