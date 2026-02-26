import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AlimentoDAO {
     private Arquivo <Alimento> arq;

     //construtor
     public AlimentoDAO () throws Exception {
          arq = new Arquivo <> ("alimentos", Alimento.class.getConstructor());
     }

     public boolean incluirAlimento (Alimento a) throws Exception {
          return arq.create(a) > 0;
     }

     public Alimento buscarAlimento (int id) throws Exception {
          return arq.read(id);
     }

     public boolean alterarAlimento (Alimento a) throws Exception {
          return arq.update(a);
     }

     public boolean excluirAlimento (int id) throws Exception {
          return arq.delete(id);
     }
}

