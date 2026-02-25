import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ClienteDAO {
     private Arquivo <Cliente> arq;

     //construtores
     public ClienteDAO () throws Exception {
          arq = new Arquivo <> ("clientes", Cliente.class.getConstructor());
     }

     public boolean incluirCliente (Cliente c) throws Exception {
          return arq.create(c) > 0;
     }

     public Cliente buscarCliente (int id) throws Exception {
          return arq.read(id);
     }

     public boolean alterarCliente (Cliente c) throws Exception {
          return arq.update(c);
     }

     public boolean excluirCliente (int id) throws Exception {
          return arq.delete(id);
     }
}

