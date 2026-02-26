package io.nexus.economizze.pessoa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulario de cadastro e edicao de pessoa.
 */
@Getter
@Setter
public class PessoaForm {

    @NotBlank(message = "Nome e obrigatorio")
    @Size(max = 150)
    private String nome;

    @NotBlank(message = "CPF e obrigatorio")
    @Pattern(regexp = "^[0-9.\\-]{11,14}$", message = "CPF invalido")
    private String cpf;

    @Size(max = 180)
    private String logradouro;

    @Size(max = 30)
    private String numero;

    @Size(max = 120)
    private String complemento;

    @Size(max = 180)
    private String pontoReferencia;

    @Size(max = 120)
    private String bairro;

    @Size(max = 120)
    private String cidade;

    @Size(max = 60)
    private String estado;

    @Pattern(regexp = "^$|^[0-9\\-]{8,9}$", message = "CEP invalido")
    private String cep;

    @Pattern(regexp = "^$|^[0-9()\\-\\s]{10,16}$", message = "Telefone celular invalido")
    private String telefoneCelular;

    @Pattern(regexp = "^$|^[0-9()\\-\\s]{10,16}$", message = "Whatsapp invalido")
    private String whatsapp;

    @Email(message = "E-mail invalido")
    @Size(max = 180)
    private String email;
}
