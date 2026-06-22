package service;

import dao.ClienteDAO;
import model.Cliente;

public class ClienteService {

    private ClienteDAO dao;

    public ClienteService() throws Exception {
        dao = new ClienteDAO();
    }

    public boolean incluir(Cliente c) throws Exception {
        return dao.incluirCliente(c);
    }

    public Cliente buscarID(int id) throws Exception {
        return dao.buscarClienteID(id);
    }

    public Cliente[] listar() throws Exception {
        return dao.listarClientes();
    }
}
