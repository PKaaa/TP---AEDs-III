public class ClienteDAO {
     private Arquivo <Cliente> arq;

     //construtores
     public ClienteDAO () throws Exception {
          arq = new Arquivo <> ("clientes", Cliente.class.getConstructor());
     }

     public boolean incluirCliente (Cliente c) throws Exception {
          return arq.create(c) > 0;
     }

     public Cliente buscarClienteID (int id) throws Exception {
          return arq.read(id);
     }

     public Cliente buscarClienteNome (String nome) throws Exception {
          for (Cliente c : arq.readAll()) {
               if (c.getNome().equalsIgnoreCase(nome)) {
                    return c;
               }
          }
          return null;
     }

     public boolean alterarCliente (Cliente c) throws Exception {
          return arq.update(c);
     }

     public boolean excluirCliente (int id) throws Exception {
          return arq.delete(id);
     }
}
