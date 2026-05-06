package view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import dao.AlimentoDAO;
import dao.ReceitaDAO;
import dao.ReceitaAlimentoDAO;
import model.Alimento;
import model.Receita;

public class MenuAlimento {
     private AlimentoDAO alimentoDAO;
     private ReceitaAlimentoDAO receitaAlimentoDAO;
     private ReceitaDAO receitaDAO;
     //private MenuReceitas menuReceitas = new MenuReceitas();
     private Scanner console = new Scanner (System.in);
     
     public MenuAlimento () throws Exception {
          alimentoDAO = new AlimentoDAO();
          receitaAlimentoDAO = new ReceitaAlimentoDAO();
          receitaDAO = new ReceitaDAO();
     }

     void menu() throws Exception {
          int op;

          do {
               System.out.println ("\n\nInicio > Alimentos");
               System.out.println ("\n\n1 - Adicionar Alimento");
               System.out.println ("\n2 - Buscar Alimento");
               System.out.println ("\n3 - Alterar Alimento");
               System.out.println ("\n4 - Excluir Alimento");
               System.out.println("\n5 - Listar Alimentos");
               System.out.println ("\n6 - Listar as receitas que utilizam determinado alimento.");
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
                    case 5:
                         listarAlimentos();
                         break;
                    case 6:
                         listarReceitasAlimento();
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
                         System.out.println("\n\nDigite a categoria do alimento: ");
                         String categoria = console.nextLine();

                         ArrayList<Alimento> lista = alimentoDAO.buscarAlimentoCategoria(categoria);

                         if (!lista.isEmpty()) {
                              for (Alimento alimento : lista) {
                                   System.out.println(alimento);
                              }
                         } else {
                              System.out.println("\nAlimento nao encontrado.");
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

     protected void incluirAlimento() throws Exception {
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
               String[] categoriasCruas = console.nextLine().split(",");
               a.setNome(nome);
               String[] categoriasLimpas = new String[categoriasCruas.length];
               
               for (int i = 0; i < categoriasCruas.length; i++) {
                    categoriasLimpas[i] = categoriasCruas[i].trim();
               }
               
               a.setCategoria(categoriasLimpas);

               if (alimentoDAO.alterarAlimento(a)) {
                    System.out.println ("\nAlimento alterado com sucesso!");
               } else {
                    System.out.println ("\nErro ao alterar alimento.");
               }
          } else {
               System.out.println ("\nAlimento nao encontrado.");
          }
     }

     protected void excluirAlimento() throws Exception {
          System.out.println ("\n\nDigite o ID do alimento: ");
          int id = console.nextInt();
          console.nextLine();

          if (alimentoDAO.excluirAlimento(id)) {
               System.out.println ("\nAlimento excluido com sucesso!");
          } else {
               System.out.println ("\nErro ao excluir alimento.");
          }
     }

     private void listarAlimentos() throws Exception {
          try {
               Alimento[] alimentos = alimentoDAO.listarAlimentos();
               if (alimentos.length == 0) System.out.println("\nNenhum alimento cadastrado/encontrado.");

               System.out.println("\nAlimentos ordenados pelo ID:");
               for (Alimento a : alimentos) System.out.println(a);
               System.out.println("\nTotal de " + alimentos.length + " alimento(s) encontrados.");
          } catch (Exception e) {
               System.err.println("Erro ao listar alimentos." + e.getMessage());
               e.printStackTrace();
          }
     }

     private void listarReceitasAlimento() throws Exception {
          System.out.println("\nDigite o ID do alimento: ");
          int idA = console.nextInt();
          console.nextLine();

          Alimento a = alimentoDAO.buscarAlimentoID(idA);
          if (a == null) {
               System.out.println("\nAlimento nao encontrado.");
               return;
          }

          int[] idsReceitas = receitaAlimentoDAO.listarReceitasAlimento(idA);
          if (idsReceitas.length == 0) {
               System.out.println("\nNenhuma receita utiliza este alimento.");
               return;
          }

          for (int idR : idsReceitas) {
               Receita r = receitaDAO.buscarReceitaID(idA);
               if (r != null) System.out.println(r);
          }
          System.out.println("\nTotal de " + idsReceitas.length + " receita(s) encontrada(s) com o alimento " + a.getNome() + ".");
     }
}
