package TP;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReceitaDAO {
     private Arquivo <Receita> arq;

     public ReceitaDAO () throws Exception {
          arq = new Arquivo <> ("receitas", Receita.class.getConstructor());
     }

     public boolean incluirReceita (Receita r) throws Exception {
          return arq.create(r) > 0;
     }

     public Receita buscarReceita (int id) throws Exception {
          return arq.read(id);
     }

     public boolean alterarReceita (Receita r) throws Exception {
          return arq.update(r);
     }

     public boolean excluirReceita (int id) throws Exception {
          return arq.delete(id);
     }
}
