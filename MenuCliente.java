import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class MenuCliente {
     private ClienteDAO clienteDAO;
     private Scanner console = new Scanner (System.in);

     public MenuCliente () throws Exception {
          clienteDAO = new ClienteDAO();
     }

     public void menu() {
          int op;

          do {
               System.out.println ("\n\nInicio > Clientes");
               System.out.println ("\n\n1 - Incluir Cliente.");
               System.out.println ("\n2 - Buscar Cliente.");
               System.out.println ("\n3 - Alterar Cliente.");
               System.out.println ("\n4 - Excluir Cliente.");
               System.out.println ("\n0 - Voltar/Sair.");
               System.out.println ("\n\nOpção: ");

               try {
                    op = Integer.valueOf(console.nextLine());
               } catch (NumberFormatException e) {
                    op = -1;
               }

               switch (op) {
                    case 1:
                         incluirCliente();
                         break;
                    case 2:
                         buscarCliente();
                         break;
                    case 3:
                         alterarCliente();
                         break;
                    case 4:
                         excluirCliente();
                         break;
                    default:
                         System.out.println ("\nOpção inválida!");
                         break;
               }
          } while (op != 0);
     }

     private void buscarCliente() {
          System.out.println ("\n\nDigite o ID do cliente: ");
          int id = console.nextInt();
          console.nextLine();

          try {
               Cliente c = clienteDAO.buscarCliente(id); 

               if (c != null) {
                    System.out.println (c);
               } else {
                    System.out.println("\nCliente nao encontrado.");
               }
          } catch (Exception e) {
               System.out.println ("\nErro ao buscar cliente.");
          }
     }

     private void incluirCliente() {
          System.out.println ("\n\nDigite o nome do cliente: ");
          String nome = console.nextLine();
          System.out.println ("\nData de Nasciemento (dd/MM/aaaa): ");
          String dataNascimentoStr = console.nextLine();
          LocalDate nascimento = LocalDate.parse (dataNascimentoStr, DateTimeFormatter.ofPattern ("dd/MM/yyyy"));

          try {
               Cliente c = new Cliente (nome, nascimento, LocalDate.now());
               clienteDAO.incluirCliente(c);
               System.out.println("\nCliente incluído com sucesso!");
          } catch (Exception e) {
               System.out.println("\nErro ao incluir cliente.");
          }
     }

     private void alterarCliente() {
          System.out.println ("\n\nDigite o ID do cliente: ");

          int id = console.nextInt();
          console.nextLine();

          try {
               Cliente c = clienteDAO.buscarCliente(id);

               if (c == null) {
                    System.out.println("Cliente nao encontrado.");
                    return;
               } 

               System.out.println ("Digite o nome do cliente: ");
               String nome = console.nextLine();
               if (!nome.isEmpty()) c.setNome(nome);

               System.out.println("Data de Nasciemento (dd/MM/aaaa): ");
               String dataNascimentoStr = console.nextLine();
               if (!dataNascimentoStr.isEmpty()) {
                    LocalDate nascimento = LocalDate.parse (dataNascimentoStr, DateTimeFormatter.ofPattern ("dd/MM/yyyy"));
                    c.setDataNascimento(nascimento);
               }

               System.out.println("Gostaria de alterar a senha? (S/N): ");
               char alterarSenha = console.nextLine().toUpperCase().charAt(0);
               if (alterarSenha == 'S' || alterarSenha == `s) {
                    System.out.println ("Digite a nova senha: ");
                    String senha = console.nextLine();
                    c.setSenha(senha);
               }

               if (clienteDAO.alterarCliente(c)) System.out.println("Cliente alterado com sucesso!");
               else System.out.println("Erro ao alterar o cliente.");
          } catch (Exception e) {
               System.err.println ("Erro ao alterar o cliente");
               e.getMessage();
          }
     }

     private void excluirCliente() {
          System.out.println ("\n\nDigite o ID do cliente: ");
          int id = console.nextInt();
          console.nextLine();

          try {
                Cliente c = clienteDAO.buscarCliente(id);
                if (c == null) {
                    System.out.println("Cliente não encontrado.");
                    return;
               }

               System.out.print ("Tem certeza que deseja excluir o cliente " + c.getNome() + "? (S/N): ");
               char confirmar = console.nextLine().toUpperCase().charAt(0);
               if (confirmar == 'S' || confirmar == 's') {
                    if (clienteDAO.excluirCliente(id)) System.out.println ("Cliente excluido com sucesso.");
                    else System.out.println ("Erro ao excluir o cliente.");
               }
          } catch (Exception e) {
               System.err.println ("Erro ao excluir o cliente");
               e.getMessage();
          }
     }
}

