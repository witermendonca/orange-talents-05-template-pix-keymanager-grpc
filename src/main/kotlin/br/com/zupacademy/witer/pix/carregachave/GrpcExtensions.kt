package br.com.zupacademy.witer.pix.carregachave

import br.com.zupacademy.witer.CarregaChavePixRequest
import br.com.zupacademy.witer.CarregaChavePixRequest.FiltroCase.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun CarregaChavePixRequest.toModel(validator: Validator): Filtro{

    val filtro = when(filtroCase!!){
        PIXECLIENTEID -> pixEClienteId.let {
            Filtro.PorPixEClenteId(clienteId= it.clienteId, pixId= it.pixId)
        }
        CHAVE -> Filtro.PorChave(chave)
        FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations);
    }

    return filtro
}