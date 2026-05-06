package view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import dao.ReceitaDAO;
import dao.AlimentoDAO;
import dao.ReceitaAlimentoDAO;
import model.Receita;
import model.Alimento;

public class MenuReceitas {
     private ReceitaDAO receitaDAO;
     private AlimentoDAO alimentoDAO;
     private ReceitaAlimentoDAO receitaAlimentoDAO;
     private MenuAlimento menuAlimento = new MenuAlimento();
     private Scanner console = new Scanner (System.in);

     public MenuReceitas() throws Exception {
          receitaDAO = new ReceitaDAO();
          alimentoDAO = new AlimentoDAO();
          receitaAlimentoDAO = new ReceitaAlimentoDAO();

     }

     public void menu() throws Exception {
          int op;

          do {
               System.out.println ("\n\nInicio > Receitas");
               System.out.println ("\n\n1 - Adicionar Receita");
               System.out.println ("\n2- Buscar Receita");
               System.out.println ("\n3 - Alterar Receita");
               System.out.println ("\n4 - Excluir Receita");
               System.out.println ("\n5 - Listar Receitas");
               System.out.println ("\n6 - Adicionar um alimento a uma receita");
               System.out.println ("\n7 - Remover um alimento de uma receita");
               System.out.println ("\n8 - Listar os alimentos de uma receita");
               System.out.println ("\n0 - Voltar/Sair");
               System.out.print("\nOpção: ");

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
                    case 5:
                         listarReceitas();
                         break;
                    case 6:
                         incluirAlimentoReceita();
                         break;
                    case 7:
                         excluirAlimentoReceita();
                         break;
                    case 8:
                         listarAlimentosReceita();
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

     protected void incluirReceita() throws Exception {
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

          System.out.println("\n\nComo deseja buscar a receita?");
          System.out.println("\n1 - ID");
          System.out.println("\n2 - Título");
          System.out.print("\nOpção: ");

          int tipoBusca = console.nextInt();
          console.nextLine();

          Receita r = null;

          switch (tipoBusca) {
               case 1:
                    System.out.println("\nDigite o ID da receita: ");
                    int id = console.nextInt();
                    console.nextLine();
                    r = receitaDAO.buscarReceitaID(id);
                    break;

               case 2:
                    System.out.println("\nDigite o título da receita: ");
                    String nome = console.nextLine();
                    r = receitaDAO.buscarReceitaTitulo(nome);
                    break;

               default:
                    System.out.println("\nOpção inválida!");
                    return;
          }

          if (r == null) {
               System.out.println("\nReceita não encontrada.");
               return;
          }

          System.out.println("\nReceita encontrada:");
          System.out.println(r);

          // Menu de alteração
          System.out.println("\n\nO que deseja alterar?");
          System.out.println("\n1 - Título");
          System.out.println("\n2 - Informações");
          System.out.println("\n3 - Tempo de preparo");
          System.out.println("\n4 - Porção");
          System.out.print("\nOpção: ");

          int op = console.nextInt();
          console.nextLine();

          switch (op) {
               case 1:
                    System.out.println("\nNovo título: ");
                    r.setTitulo(console.nextLine());
                    break;

               case 2:
                    System.out.println("\nNovas informações: ");
                    r.setInformacoes(console.nextLine());
                    break;

               case 3:
                    System.out.println("\nNovo tempo (min): ");
                    r.setTempoPreparo(console.nextInt());
                    console.nextLine();
                    break;

               case 4:
                    System.out.println("\nNova porção: ");
                    r.setPorcao(console.nextLine());
                    break;

               default:
                    System.out.println("\nOpção inválida!");
                    return;
          }

          if (receitaDAO.alterarReceita(r)) {
               System.out.println("\nReceita alterada com sucesso!");
          } else {
               System.out.println("\nErro ao alterar receita.");
          }
     }

     protected void excluirReceita() throws Exception {
          System.out.println ("\n\nDigite o ID da receita que voce gostaria de excluir: ");
          int id = console.nextInt();
          console.nextLine();
          if (receitaDAO.excluirReceita(id)) {
               System.out.println ("\nReceita excluída com sucesso!");
          } else {
               System.out.println ("\nErro ao excluir receita.");
          }
     }

     private void listarReceitas() throws Exception {
          try {
               Receita[] receitas = receitaDAO.listarReceitas();
               if (receitas.length == 0) System.out.println("\nNenhuma receita cadastrada/encontrada.");

               System.out.println("\nReceitas ordenadas pelo ID: ");
               for (Receita r : receitas) System.out.println(r);
               System.out.println("\nTotal de " + receitas.length + " receita(s) encontrada(s).");
          } catch (Exception e) {
               System.err.println("Erro ao listar as receitas.");
               e.printStackTrace();
          }
     }

     private void incluirAlimentoReceita() throws Exception {
          System.out.println ("\n\nDigite o ID da receita: ");
          int idR = console.nextInt();
          console.nextLine();

          Receita r = receitaDAO.buscarReceitaID(idR);
          if (r == null) {
               System.out.println("\nReceita nao encontrada.");
               return;
          }
          System.out.println ("\nReceita: " + r.getTitulo());

          System.out.println ("\n\nO alimento que voce deseja adicionar ja existe? (S/N): ");
          String resp = console.nextLine().trim();
          if (resp.equals("S") || resp.equals("s")) {
               System.out.println ("\n\nDigite o ID do alimento: ");
               int idA = console.nextInt();
               console.nextLine();

               Alimento a = alimentoDAO.buscarAlimentoID(idA);
               if (a == null) {
                    System.out.println("\nAlimento nao encontrado/nao existe.");
                    return;
               }

               if (receitaAlimentoDAO.createRelacao(idR, idA)) System.out.println("\nAlimento adicionado a receita com sucesso!");
               else System.out.println("\nErro ao adicionar alimento a receita.");
          } else {
               menuAlimento.incluirAlimento();
               incluirAlimentoReceita(); //chama o método novamente para adicionar o alimento recém criado
          }
     }

     private void excluirAlimentoReceita() throws Exception {
          System.out.println ("\n\nDigite o ID da receita que voce gostaria de excluir: ");
          int idR = console.nextInt();
          console.nextLine();

          Receita r = receitaDAO.buscarReceitaID(idR);
          if (r == null) {
               System.out.println("\nReceita nao encontrada.");
               return;
          }
          
          System.out.println ("\nAlimentos na receita " + r.getTitulo() + ":"); //facilita a escolha de qual alimento ira excluir
          int[] idsAlimentos = receitaAlimentoDAO.listarAlimentosReceita(idR);
          if (idsAlimentos.length == 0) {
               System.out.println("\nNenhum alimento encontrado para esta receita.");
               return;
          }

          for (int idA : idsAlimentos) {
               Alimento a = alimentoDAO.buscarAlimentoID(idA);
               if (a != null) System.out.println(a + " ");
          }
          System.out.println("\nDigite o ID do alimento que voce gostaria de excluir da receita " + r.getTitulo() + ": ");
          int idA = console.nextInt();
          console.nextLine();

          if (receitaAlimentoDAO.deleteRelacao(idR, idA)) System.out.println("\nAlimento excluido com sucesso!");
          else System.out.println("\nErro ao excluir alimento da receita.");
     }

     private void listarAlimentosReceita() throws Exception {
          System.out.println ("\n\nDigite o ID da receita: ");
          int idR = console.nextInt();
          console.nextLine();

          Receita r = receitaDAO.buscarReceitaID(idR);
          if (r == null) {
               System.out.println("\nReceita nao encontrada.");
               return;
          }

          System.out.println("\nAlimentos na receita " + r.getTitulo() + ":");
          int[] idsAlimentos = receitaAlimentoDAO.listarAlimentosReceita(idR);
          if (idsAlimentos.length == 0) {
               System.out.println("\nNenhum alimento encontrado para esta receita.");
               return;
          }
          for (int idA : idsAlimentos) {
               Alimento a = alimentoDAO.buscarAlimentoID(idA);
               if (a != null) System.out.println(a);
          }
          System.out.println("\nTotal de " + idsAlimentos.length + " alimento(s) encontrado(s) para a receita " + r.getTitulo() + ".");
     }
}


