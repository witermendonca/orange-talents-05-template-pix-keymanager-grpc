package br.com.zupacademy.witer.externo

import br.com.zupacademy.witer.pix.ContaAssociada
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import java.util.*

@Client("\${itau.url}")
interface ItauClient {

    @Get("/api/v1/clientes/{clienteId}/contas{?tipo}")
    fun buscaContaClienteIdETipo(@PathVariable clienteId: String, @QueryValue tipo: String): HttpResponse<DadosDaContaResponse>
}


data class DadosDaContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {

    fun toModel(): ContaAssociada {
        return ContaAssociada(
            instituicao = this.instituicao.nome,
            nomeDoTitular = this.titular.nome,
            cpfDoTitular = this.titular.cpf,
            agencia = this.agencia,
            numeroDaConta = this.numero
        )
    }

}

data class TitularResponse(val id: UUID, val nome: String, val cpf: String)
data class InstituicaoResponse(val nome: String, val ispb: String)

//ex: json
//{
//    "tipo": "CONTA_CORRENTE",
//    "instituicao": {
//    "nome": "ITAÚ UNIBANCO S.A.",
//    "ispb": "60701190"
//},
//    "agencia": "0001",
//    "numero": "291900",
//    "titular": {
//    "id": "c56dfef4-7901-44fb-84e2-a2cefb157890",
//    "nome": "Rafael M C Ponte",
//    "cpf": "00000000000"
//}
//}