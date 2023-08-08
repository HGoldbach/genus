package br.goldbach.clienteservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO implements Serializable {
    private Long id;
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
}
