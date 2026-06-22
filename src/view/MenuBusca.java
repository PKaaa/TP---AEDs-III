package view;

import java.util.*;

import model.Alimento;
import model.Cliente;
import model.Receita;
import service.BuscaService;

public class MenuBusca {
     private BuscaService buscaService;
     private Scanner console = new Scanner(System.in);

     public MenuBusca() throws Exception {
          buscaService = new BuscaService();
     }

     public void menu() throws Exception {
          int op;

          do {
               System.out.println("\n\nInicio > Pesquisar por padrão (KMP / BM)");
               System.out.println("\n1 - Buscar Cliente");
               System.out.println("\n2 - Buscar Alimento");
               System.out.println("\n3 - Buscar Receita");
               System.out.println("\n0 - Voltar/Sair");
               System.out.print("\nOpção: ");

               try {
                    op = Integer.valueOf(console.nextLine());
               } catch (NumberFormatException e) {
                    op = -1;
               }

               switch (op) {
                    case 1: buscarCliente();  break;
                    case 2: buscarAlimento(); break;
                    case 3: buscarReceita();  break;
                    case 0: System.out.println("Saindo..."); break;
                    default: System.out.println("\nOpção inválida!"); break;
               }
          } while (op != 0);
     }

     //escolha do algoritmo (compartilhado pelas três buscas)
     private String escolherAlgoritmo() {
          System.out.println("\nQual algoritmo deseja utilizar?");
          System.out.println("\n1 - KMP (Knuth-Morris-Pratt)");
          System.out.println("\n2 - Boyer-Moore");
          System.out.print("\nOpção: ");

          String op = console.nextLine().trim();
          return op.equals("2") ? "BM" : "KMP"; //default para KMP em caso de entrada inválida
     }

     private void buscarCliente() throws Exception {
          String alg = escolherAlgoritmo();

          System.out.print("\nDigite o padrão a buscar no nome do cliente: ");
          String padrao = console.nextLine();

          ArrayList<Cliente> resultado = buscaService.buscarCliente(padrao, alg);

          if (resultado.isEmpty()) {
               System.out.println("\nNenhum cliente encontrado com o padrão \"" + padrao + "\".");
               return;
          }

          System.out.println("\n--- Resultado da busca (" + alg + ") ---");
          for (Cliente c : resultado) System.out.println(c);
          System.out.println("\nTotal: " + resultado.size() + " cliente(s) encontrado(s).");
     }

     private void buscarAlimento() throws Exception {
          String alg = escolherAlgoritmo();

          System.out.print("\nDigite o padrão a buscar no nome do alimento: ");
          String padrao = console.nextLine();

          ArrayList<Alimento> resultado = buscaService.buscarAlimento(padrao, alg);

          if (resultado.isEmpty()) {
               System.out.println("\nNenhum alimento encontrado com o padrão \"" + padrao + "\".");
               return;
          }

          System.out.println("\n--- Resultado da busca (" + alg + ") ---");
          for (Alimento a : resultado) System.out.println(a);
          System.out.println("\nTotal: " + resultado.size() + " alimento(s) encontrado(s).");
     }

     private void buscarReceita() throws Exception {
          String alg = escolherAlgoritmo();

          System.out.print("\nDigite o padrão a buscar no título da receita: ");
          String padrao = console.nextLine();

          ArrayList<Receita> resultado = buscaService.buscarReceita(padrao, alg);

          if (resultado.isEmpty()) {
               System.out.println("\nNenhuma receita encontrada com o padrão \"" + padrao + "\".");
               return;
          }

          System.out.println("\n--- Resultado da busca (" + alg + ") ---");
          for (Receita r : resultado) System.out.println(r);
          System.out.println("\nTotal: " + resultado.size() + " receita(s) encontrada(s).");
     }
}
