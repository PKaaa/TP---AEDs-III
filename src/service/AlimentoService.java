package service;

import dao.AlimentoDAO;
import model.Alimento;

public class AlimentoService {

    private AlimentoDAO dao;

    public AlimentoService() throws Exception {
        dao = new AlimentoDAO();
    }

    public boolean incluir(Alimento a) throws Exception {
        return dao.incluirAlimento(a);
    }

    public Alimento buscarID(int id) throws Exception {
        return dao.buscarAlimentoID(id);
    }

    public Alimento[] listar() throws Exception {
        return dao.listarAlimentos();
    }
}
