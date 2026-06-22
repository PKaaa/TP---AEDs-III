package dao;

import model.Cliente;
import util.Arquivo;
import util.CriptografiaXOR;

public class ClienteDAO {
    private Arquivo<Cliente> arq;

    public ClienteDAO() throws Exception {
        arq = new Arquivo<>("clientes", Cliente.class.getConstructor());
    }

    // INCLUIR COM CRIPTOGRAFIA
    public boolean incluirCliente(Cliente c) throws Exception {
        if (c.getSenha() != null && !c.getSenha().isEmpty()) {
            c.setSenha(CriptografiaXOR.criptografar(c.getSenha()));
        }
        return arq.create(c) > 0;
    }

    // ALTERAR COM CRIPTOGRAFIA
    public boolean alterarCliente(Cliente c) throws Exception {
        if (c.getSenha() != null && !c.getSenha().isEmpty() 
            && !CriptografiaXOR.isCriptografado(c.getSenha())) {
            c.setSenha(CriptografiaXOR.criptografar(c.getSenha()));
        }
        return arq.update(c);
    }

    // AUTENTICAÇÃO COM DESCRIPTOGRAFIA
    public Cliente autenticarCliente(String email, String senha) throws Exception {
        for (Cliente c : arq.readAll()) {
            if (c.getEmail() != null) {
                for (String e : c.getEmail()) {
                    if (e != null && e.equalsIgnoreCase(email) && c.getSenha() != null) {
                        try {
                            String senhaDescriptografada = CriptografiaXOR.descriptografar(c.getSenha());
                            if (senhaDescriptografada.equals(senha)) {
                                return c;
                            }
                        } catch (Exception ex) {
                            // Fallback: se não conseguir descriptografar, tenta comparar direto
                            if (c.getSenha().equals(senha)) {
                                return c;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    // MÉTODOS EXISTENTES
    public Cliente buscarClienteID(int id) throws Exception {
        return arq.read(id);
    }

    public Cliente buscarClienteNome(String nome) throws Exception {
        for (Cliente c : arq.readAll()) {
            if (c.getNome() != null && c.getNome().equalsIgnoreCase(nome)) {
                return c;
            }
        }
        return null;
    }

    public Cliente[] listarClientes() throws Exception {
        return arq.readAll();
    }

    public Cliente[] listarClientesOrdenados() throws Exception {
        return arq.readAllArvB();
    }

    public boolean excluirCliente(int id) throws Exception {
        return arq.delete(id);
    }
}
