package service;

import dao.*;
import model.*;
import service.busca.KMP;
import service.busca.BoyerMoore;

import java.util.ArrayList;

public class BuscaService {

    private AlimentoDAO alimentoDAO;
    private ClienteDAO clienteDAO;
    private ReceitaDAO receitaDAO;

    private KMP kmp;
    private BoyerMoore bm;

    public BuscaService() throws Exception {
        alimentoDAO = new AlimentoDAO();
        clienteDAO = new ClienteDAO();
        receitaDAO = new ReceitaDAO();

        kmp = new KMP();
        bm = new BoyerMoore();
    }

    // =========================
    // ESCOLHA DO ALGORITMO
    // =========================

    private boolean match(String text, String pattern, String alg) {

        if (alg.equalsIgnoreCase("KMP")) {
            return kmp.contains(text, pattern);
        }

        return bm.contains(text, pattern);
    }

    // =========================
    // ALIMENTO
    // =========================

    public ArrayList<Alimento> buscarAlimento(String pattern, String alg) throws Exception {

        ArrayList<Alimento> res = new ArrayList<>();

        for (Alimento a : alimentoDAO.listarAlimentos()) {

            if (a.getNome() != null &&
                match(a.getNome(), pattern, alg)) {
                res.add(a);
            }
        }

        return res;
    }

    // =========================
    // CLIENTE
    // =========================

    public ArrayList<Cliente> buscarCliente(String pattern, String alg) throws Exception {

        ArrayList<Cliente> res = new ArrayList<>();

        for (Cliente c : clienteDAO.listarClientes()) {

            if (c.getNome() != null &&
                match(c.getNome(), pattern, alg)) {
                res.add(c);
            }
        }

        return res;
    }

    // =========================
    // RECEITA
    // =========================

    public ArrayList<Receita> buscarReceita(String pattern, String alg) throws Exception {

        ArrayList<Receita> res = new ArrayList<>();

        for (Receita r : receitaDAO.listarReceitas()) {

            if (r.getTitulo() != null &&
                match(r.getTitulo(), pattern, alg)) {
                res.add(r);
            }
        }

        return res;
    }
}
