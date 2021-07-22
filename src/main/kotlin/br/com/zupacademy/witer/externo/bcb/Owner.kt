package br.com.zupacademy.witer.externo.bcb

data class Owner(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String,
) {
    enum class OwnerType {
        NATURAL_PERSON,
        LEGAL_PERSON
    }
}

//OwnerRequest{
//    type*	string
//    Enum:
//    [ NATURAL_PERSON, LEGAL_PERSON ]
//    name*	string
//    example: Steve Jobs
//    Nome completo
//
//            taxIdNumber*	string
//    example: 33059192057
//    CPF - Cadastro de Pessoa Física
//
//}