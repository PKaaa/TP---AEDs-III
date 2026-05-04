package dao;

import model.Receita;
import util.Arquivo;

public class ReceitaDAO {
     private Arquivo <Receita> arq;

     public ReceitaDAO () throws Exception {
          arq = new Arquivo <> ("receitas", Receita.class.getConstructor());
     }

     public boolean incluirReceita (Receita r) throws Exception {
          return arq.create(r) > 0;
     }

     public Receita buscarReceitaID (int id) throws Exception {
          return arq.read(id);
     }

     public Receita buscarReceitaTitulo (String nome) throws Exception {
          for (Receita r : arq.readAll()) {
               if (r.getTitulo().equalsIgnoreCase(nome)) {
                    return r;
               }
          }
          return null;
     }

     public Receita buscarReceitaTempo (int tempo) throws Exception {
          for (Receita r : arq.readAll()) {
               if (r.getTempoPreparo() == tempo) {
                    return r;
               }
          }
          return null;
     }

     public Receita[] listarReceitas() throws Exception {
          return arq.readAll();
     }
     
     //listagem a partir do ID usando a arvore B
     public Receita[] listarReceitasOrdenados() throws Exception {
          return arq.readAllArvB();
     }

     public boolean alterarReceita (Receita r) throws Exception {
          return arq.update(r);
     }

     public boolean excluirReceita (int id) throws Exception {
          return arq.delete(id);
     }
}
