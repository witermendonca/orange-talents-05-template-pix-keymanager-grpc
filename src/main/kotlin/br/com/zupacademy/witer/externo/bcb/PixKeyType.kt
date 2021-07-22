package br.com.zupacademy.witer.externo.bcb

import br.com.zupacademy.witer.pix.TipoChave

enum class PixKeyType(val domainType: TipoChave?) {

    CPF(TipoChave.CPF),
    CNPJ(null),
    PHONE(TipoChave.CELULAR),
    EMAIL(TipoChave.EMAIL),
    RANDOM(TipoChave.ALEATORIA);

    companion object {
        private val mapping = PixKeyType.values().associateBy(PixKeyType::domainType)

        fun by(domainType: TipoChave): PixKeyType {
            return mapping[domainType]
                ?: throw IllegalArgumentException("PixKeyType invalid or not found for $domainType")
        }
    }

}

//keyType*	string
//example: CPF
//Tipo de chave. Novos tipos podem surgir
//
//Enum:
//[ CPF, CNPJ, PHONE, EMAIL, RANDOM ]