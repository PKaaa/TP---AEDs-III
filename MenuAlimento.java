import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MenuAlimento {
     private AlimentoDAO alimentoDAO;
     private Scanner console = new Scanner (System.in);
     
     public MenuAlimento () throws Exception {
          alimentoDAO = new AlimentoDAO();
     }

     void menu() throws Exception {
          int op;

          do {
               System.out.println ("\n\nInicio > Alimentos");
               System.out.println ("\n1 - Adicionar Alimento");
               System.out.println ("\n2 - Buscar Alimento");
               System.out.println ("\n3 - Alterar Alimento");
               System.out.println ("\n4 - Excluir Alimento");
               System.out.println ("\n0 - Voltar/Sair");
               System.out.println ("\nOpção: ");

               try {
                    op = console.nextInt();
                    console.nextLine();
               } catch (InputMismatchException e) {
                    console.nextLine();
                    op = -1;
               } catch (NumberFormatException e) {
                    op = -1;//erro
               } catch (Exception e) {
                    console.nextLine();
                    op = -1;
               }

               switch (op) {
                    case 1:
                         incluirAlimento();
                         break;
                    case 2:
                         buscarAlimento();
                         break;
                    case 3:
                         alterarAlimento();
                         break;
                    case 4:
                         excluirAlimento();
                         break;
                     case 0:
                         System.out.println("Saindo...");
                         break;
                    default:
                         System.out.println ("\nOpção inválida!");
                         break;
               }
          } while (op != 0);
     }
     
     private void buscarAlimento() throws Exception {
          System.out.println ("\n\nDigite por qual meio voce gostaria de buscar o alimento: ");
          System.out.println ("\n1 - ID");
          System.out.println ("\n2 - Nome");
          System.out.println ("\n3 - Categoria");
          int op = console.nextInt();
          console.nextLine();

          Alimento a;
          
          try {
               switch (op) {
                    case 1:
                         System.out.println ("\n\nDigite o ID do alimento: ");
                         int id = console.nextInt();
                         console.nextLine();
                         a = alimentoDAO.buscarAlimentoID(id);
                         if (a != null) {
                              System.out.println (a);
                         } else {
                              System.out.println("\nAlimento nao encontrado.");
                         }
                         break;
                    
                    case 2:
                         System.out.println ("\n\nDigite o nome do alimento: ");
                         String nome = console.nextLine();
                         a = alimentoDAO.buscarAlimentoNome(nome);

                         if (a != null) {
                              System.out.println (a);
                         } else {
                              System.out.println ("\nAlimento nao encontrado.");
                         }
                         break;

                    case 3:
                         System.out.println ("\n\nDigite a categoria do alimento: ");
                         String categoria = console.nextLine();
                         a = alimentoDAO.buscarAlimentoCategoria(categoria);

                         if (a != null) {
                              System.out.println (a);
                         } else {
                              System.out.println ("\nAlimento nao encontrado.");
                         }
                         break;

                    default:
                         System.out.println ("\nOpcao invalida!");
                         break;
               }
          } catch (NumberFormatException e) {
               op = -1;//erro
          }
     }

     private void incluirAlimento() throws Exception {
          System.out.println ("\n\nDigite o nome do alimento: ");
          String nome = console.nextLine().trim();
          System.out.println ("\n\nDigite as categorias do alimento (separadamente por virgula): ");
          
          String categoriasStr = console.nextLine();
          String[] categoriasCruas = categoriasStr.split(",");
          String[] categoriasLimpas = new String [categoriasCruas.length];

          for (int i = 0; i < categoriasCruas.length; i++) {
               categoriasLimpas[i] = categoriasCruas[i].trim();
          }
          
          Alimento a = new Alimento(0, nome, categoriasLimpas);

          if (alimentoDAO.incluirAlimento(a)) {
               System.out.println ("\nAlimento incluido com sucesso!");
          } else {
               System.out.println ("\nErro ao incluir alimento.");
          }
     }

     private void alterarAlimento() throws Exception {
          System.out.println ("\n\nDigite o ID do alimento: ");
          int id = console.nextInt();
          console.nextLine();

          Alimento a = alimentoDAO.buscarAlimentoID(id);

          if (a != null) {
               System.out.println ("\n\nDigite o nome do alimento: ");
               String nome = console.nextLine();
               System.out.println ("\n\nDigite as categorias do alimento (separadamente por virgula): ");
               String[] categorias = console.nextLine().split(",");
               a.setNome(nome);
               a.setCategoria(categorias);

               if (alimentoDAO.alterarAlimento(a)) {
                    System.out.println ("\nAlimento alterado com sucesso!");
               } else {
                    System.out.println ("\nErro ao alterar alimento.");
               }
          } else {
               System.out.println ("\nAlimento nao encontrado.");
          }
     }

     private void excluirAlimento() throws Exception {
          System.out.println ("\n\nDigite o ID do alimento: ");
          int id = console.nextInt();
          console.nextLine();

          if (alimentoDAO.excluirAlimento(id)) {
               System.out.println ("\nAlimento excluido com sucesso!");
          } else {
               System.out.println ("\nErro ao excluir alimento.");
          }
     }

}
