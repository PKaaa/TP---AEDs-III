package TP;

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
}
