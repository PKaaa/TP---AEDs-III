import java.time.LocalDate;
import java.time.format.DateTimeFormatter; //nao eh usado nenhuma dessas bibliotecas POR ENQUANTO

public class AlimentoDAO {
     private Arquivo <Alimento> arq;

     //construtor
     public AlimentoDAO () throws Exception {
          arq = new Arquivo <> ("alimentos", Alimento.class.getConstructor());
     }

     public boolean incluirAlimento (Alimento a) throws Exception {
          return arq.create(a) > 0;
     }

     public Alimento buscarAlimentoID (int id) throws Exception {
          return arq.read(id);
     }

     public Alimento buscarAlimentoNome (String nome) throws Exception {
          for (Alimento a : arq.readAll()) {
               if (a.getNome().equalsIgnoreCase(nome)) {
                    return a;
               }
          }
          return null;
     }

     public Alimento buscarAlimentoCategoria (String categoria) throws Exception {
          for (Alimento a : arq.readAll()) {
               for (String cat : a.getCategoria()) {
                    if (cat.equalsIgnoreCase(categoria)) {
                         return a;
                    }
               }
          }
          return null;
     }

     public boolean alterarAlimento (Alimento a) throws Exception {
          return arq.update(a);
     }

     public boolean excluirAlimento (int id) throws Exception {
          return arq.delete(id);
     }
}
