package dao;

import model.Alimento;
import util.Arquivo;

import java.util.ArrayList;

public class AlimentoDAO {
    private Arquivo<Alimento> arq;

    public AlimentoDAO() throws Exception {
        arq = new Arquivo<>("alimentos", Alimento.class.getConstructor());
    }

    // CREATE
    public boolean incluirAlimento(Alimento a) throws Exception {
        return arq.create(a) > 0;
    }

    // READ por ID 
    public Alimento buscarAlimentoID(int id) throws Exception {
        return arq.read(id);
    }

    // READ por nome
    public Alimento buscarAlimentoNome(String nome) throws Exception {
        for (Alimento a : arq.readAll()) {
            if (a.getNome() != null && a.getNome().equalsIgnoreCase(nome)) {
                return a;
            }
        }
        return null;
    }

    // READ por categoria
    public ArrayList<Alimento> buscarAlimentoCategoria(String categoria) throws Exception {
        ArrayList<Alimento> lista = new ArrayList<>();

        for (Alimento a : arq.readAll()) {

            String[] categorias = a.getCategoria();

            if (categorias != null) {
                for (String cat : categorias) {
                    if (cat != null && cat.trim().equalsIgnoreCase(categoria.trim())) {
                        lista.add(a);
                        break; // evita duplicar o mesmo alimento
                    }
                }
            }
        }

        return lista;
    }

    public Alimento[] listarAlimentos() throws Exception {
        return arq.readAll();
    }

    public boolean alterarAlimento(Alimento a) throws Exception {
        return arq.update(a);
    }

    public boolean excluirAlimento(int id) throws Exception {
        return arq.delete(id);
    }
}
