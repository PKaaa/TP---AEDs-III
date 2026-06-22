package service;

import dao.ReceitaDAO;
import model.Receita;

public class ReceitaService {

    private ReceitaDAO dao;

    public ReceitaService() throws Exception {
        dao = new ReceitaDAO();
    }

    public boolean incluir(Receita r) throws Exception {
        return dao.incluirReceita(r);
    }

    public Receita[] listar() throws Exception {
        return dao.listarReceitas();
    }
}
