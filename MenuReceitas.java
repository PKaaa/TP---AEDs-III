import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MenuReceitas {
     private ReceitaDAO receitaDAO;
     private Scanner console = new Scanner (System.in);

     public MenuReceitas() throws Exception {
          receitaDAO = new ReceitaDAO();
     }

     public void menu() throws Exception {
          int op;

          do {
               System.out.println ("\n\nInicio > Receitas");
               System.out.println ("\n\n1 - Adicionar Receita");
               System.out.println ("\n\n2- Buscar Receita");
               System.out.println ("\n\n3 - Alterar Receita");
               System.out.println ("\n\n4 - Excluir Receita");
               System.out.println ("\n\n0 - Voltar/Sair");

               try {
                    op = console.nextInt();
                    console.nextLine();
               } catch (NumberFormatException e) {
                    console.nextLine();
                    op = -1;//erro
               }

               switch (op) {
                    case 1:
                         incluirReceita();
                         break;
                    case 2:
                         buscarReceita();
                         break;
                    case 3:
                         alterarReceita();
                         break;
                    case 4:
                         excluirReceita();
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

     private void buscarReceita() throws Exception {
          System.out.println ("\n\nDigite por qual meio voce gostaria de buscar a receita: ");
          System.out.println ("\n1 - ID");
          System.out.println ("\n2 - Titulo");
          System.out.println ("\n3 - Tempo de preparo");
          int op = console.nextInt();
          console.nextLine();

          Receita r;

          try {
               switch (op) {
                    case 1:
                         System.out.println ("\n\nDigite o ID da receita: ");
                         int id = console.nextInt();
                         console.nextLine();
                         r = receitaDAO.buscarReceitaID(id);
                         if (r != null) {
                              System.out.println (r);
                         } else {
                              System.out.println("\nReceita nao encontrada.");
                         }
                         break;

                    case 2:
                         System.out.println ("\n\nDigite o nome da receita: ");
                         String nome = console.nextLine();
                         r = receitaDAO.buscarReceitaTitulo(nome);
                         if (r != null) {
                              System.out.println (r);
                         } else {
                              System.out.println("\nReceita nao encontrada.");
                         }
                         break;
                    
                    case 3:
                         System.out.println ("\n\nDigite o tempo de preparo da receita: ");
                         int tempo = console.nextInt();
                         console.nextLine();
                         r = receitaDAO.buscarReceitaTempo(tempo);
                         if (r != null) {
                              System.out.println (r);
                         } else {
                              System.out.println("\nReceita nao encontrada.");
                         }
                         break;

                    default:
                         System.out.println ("\nOpção inválida!");
                         break;
               }
          } catch (NumberFormatException e) {
               System.out.println ("\nErro ao buscar receita.");
          }
     }

     private void incluirReceita() throws Exception {
          System.out.println ("\n\nDigite o nome da receita: ");
          String nome = console.nextLine().trim();

          System.out.println ("\n\nDigite as informações da receita: ");
          String informacoes = console.nextLine();

          System.out.println ("\n\nDigite o tempo de preparo da receita (em minutos): ");
          int tempo = console.nextInt();
          console.nextLine();

          System.out.println ("\n\nDigite a porção da receita: ");
          String porcao = console.nextLine();
          
          Receita r = new Receita(nome, informacoes, tempo, porcao);
          if (receitaDAO.incluirReceita(r)) {
               System.out.println ("\nReceita incluída com sucesso!");
          } else {
               System.out.println ("\nErro ao incluir receita.");
          }
     }

     private void alterarReceita() throws Exception {
          System.out.println ("\n\nDigite o que voce gostaria de alterar: ");
          System.out.println ("\n1 - Titulo");
          System.out.println ("\n2 - Informações");
          System.out.println ("\n3 - Tempo de preparo");
          System.out.println ("\n4 - Porção");
          int op = console.nextInt();
          console.nextLine();

          System.out.println ("\n\nDigite o ID da receita que voce gostaria de alterar: ");
          int id = console.nextInt();
          console.nextLine();
          try {
               switch (op) {
                    case 1:
                         System.out.println ("\n\nDigite o novo titulo da receita: ");
                         String nome = console.nextLine();
                         Receita r = receitaDAO.buscarReceitaID(id);
                         if (r != null) {
                              r.setTitulo(nome);
                              if (receitaDAO.alterarReceita(r)) {
                                   System.out.println ("\nReceita alterada com sucesso!");
                              } else {
                                   System.out.println ("\nErro ao alterar receita.");
                              }
                         } else {
                              System.out.println("\nReceita nao encontrada.");
                         }
                         break;
                    case 2:
                         System.out.println ("\n\nDigite as novas informações da receita: ");
                         String informacoes = console.nextLine();
                         r = receitaDAO.buscarReceitaID(id);
                         if (r != null) {
                              r.setInformacoes(informacoes);
                              if (receitaDAO.alterarReceita(r)) {
                                   System.out.println ("\nReceita alterada com sucesso!");
                              } else {
                                   System.out.println ("\nErro ao alterar receita.");
                              }
                         } else {
                              System.out.println("\nReceita nao encontrada.");
                         }
                         break;
                    case 3:
                         System.out.println ("\n\nDigite o novo tempo de preparo da receita (em minutos): ");
                         int tempo = console.nextInt();
                         console.nextLine();
                         r = receitaDAO.buscarReceitaID(id);
                         if (r != null) {
                              r.setTempoPreparo(tempo);
                              if (receitaDAO.alterarReceita(r)) {
                                   System.out.println ("\nReceita alterada com sucesso!");
                              } else {
                                   System.out.println ("\nErro ao alterar receita.");
                              }
                         } else {
                              System.out.println("\nReceita nao encontrada.");
                         }
                         break;
                     case 4:
                         System.out.println ("\n\nDigite a nova porção da receita: ");
                         String porcao = console.nextLine();

                         r = receitaDAO.buscarReceitaID(id);
                         if (r != null) {
                              r.setPorcao(porcao);
                              if (receitaDAO.alterarReceita(r)) {
                                   System.out.println ("\nReceita alterada com sucesso!");
                              } else {
                                   System.out.println ("\nErro ao alterar receita.");
                              }
                         } else {
                              System.out.println("\nReceita nao encontrada.");
                         }
                         break;
                    default:
                         System.out.println ("\nOpção inválida!");
                         break;
               }
          } catch (NumberFormatException e) {
               System.out.println ("\nErro ao alterar receita.");
          }
     }

     private void excluirReceita() throws Exception {
          System.out.println ("\n\nDigite o ID da receita que voce gostaria de excluir: ");
          int id = console.nextInt();
          console.nextLine();
          if (receitaDAO.excluirReceita(id)) {
               System.out.println ("\nReceita excluída com sucesso!");
          } else {
               System.out.println ("\nErro ao excluir receita.");
          }
     }

}

