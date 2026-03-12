import java.util.*;

public class Principal {
     public static void main (String[] args) throws Exception {
          Scanner console = new Scanner (System.in);
          int opcao;

          try {
               do {
                    System.out.println("\n\nAEDsIII");
                    System.out.println("-------");
                    System.out.println("> Início");
                    System.out.println("\n1 - Clientes");
                    System.out.println("\n2 - Alimentos");
                    System.out.println("\n3 - Receitas");
                    System.out.println("\n0 - Sair");
                    System.out.print("\nOpção: ");
                    
                    try {
                         opcao = Integer.valueOf(console.nextLine());
                    } catch (NumberFormatException e) {
                         opcao = -1;
                    }

                    switch (opcao) {
                         case 1:
                         MenuCliente menuClientes = new MenuCliente();
                         menuClientes.menu();
                         break;
                         case 2:
                              MenuAlimento menuAlimentos = new MenuAlimento();
                              menuAlimentos.menu();
                              break;
                         case 3:
                              MenuReceitas menuReceitas = new MenuReceitas();
                              menuReceitas.menu();
                              break;
                         case 0:
                         System.out.println("Saindo...");
                         break;
                         default:
                         System.out.println("Opção inválida!");
                         break;
                    }
               } while (opcao != 0);
          } catch (Exception e) {
               System.err.println("Erro fatal.");
               e.printStackTrace();
          } finally {
               console.close();
          }
     }
}
