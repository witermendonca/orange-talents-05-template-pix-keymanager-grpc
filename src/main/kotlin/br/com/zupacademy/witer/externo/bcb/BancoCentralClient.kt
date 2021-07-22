package br.com.zupacademy.witer.externo.bcb

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.url}")
interface BancoCentralClient {

    @Post("/api/v1/pix/keys",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun createPixKeyBcb(@Body request: CreatePixKeyBcbRequest): HttpResponse<CreatePixKeyBcbResponse>


    @Delete("/api/v1/pix/keys/{key}",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun deletePixKeyBcb(
        @PathVariable("key") key: String,
        @Body request: DeletePixKeyBcbRequest,
    ): HttpResponse<DeletePixKeyBcbResponse>


    @Get("/api/v1/pix/keys/{key}",
        consumes = [MediaType.APPLICATION_XML]
    )
    fun findByPixKeyBcb(@PathVariable("key") key: String): HttpResponse<PixKeyDetailsResponse>

}

