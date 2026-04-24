package controller;

import dao.ClienteDAO;
import dao.AlimentoDAO;
import dao.ReceitaDAO;

import model.Cliente;
import model.Alimento;
import model.Receita;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.security.SecureRandom;

import static spark.Spark.*;

public class Servidor {

    private static final int PORTA = 7777;
    private static final long RESET_TOKEN_TTL_MS = 10 * 60 * 1000;
    private static final Map<String, ResetToken> resetTokens = new ConcurrentHashMap<>();
    private static final SecureRandom random = new SecureRandom();

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, t,
                            ctx) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, t, ctx) -> LocalDate.parse(json.getAsString(),
                            DateTimeFormatter.ISO_LOCAL_DATE))
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    public static void main(String[] args) throws Exception {

        // DAOs
        ClienteDAO clienteDAO = new ClienteDAO();
        AlimentoDAO alimentoDAO = new AlimentoDAO();
        ReceitaDAO receitaDAO = new ReceitaDAO();

        // Configuração do Spark
        port(PORTA);

        // Força encoding UTF-8 em todas as requisições e respostas
        before((req, res) -> {
            req.attribute("charset", "UTF-8");
            res.type("application/json; charset=UTF-8");
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type");
        });
        options("/*", (req, res) -> "OK");

        // ROTA RAIZ
        get("/", (req, res) -> {
            res.type("application/json");
            return "{\"status\":\"NutriChef API online\",\"porta\":" + PORTA + "}";
        });

        // GET /clientes — listar todos
        get("/clientes", (req, res) -> {
            res.type("application/json");
            try {
                Cliente[] lista = clienteDAO.listarClientes();
                return gson.toJson(lista);
            } catch (Exception e) {
                res.status(500);
                return erro(e.getMessage());
            }
        });

        // GET /clientes/:id — buscar por ID
        get("/clientes/:id", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params(":id"));
                Cliente c = clienteDAO.buscarClienteID(id);
                if (c != null)
                    return gson.toJson(c);
                res.status(404);
                return erro("Cliente não encontrado");
            } catch (Exception e) {
                res.status(500);
                return erro(e.getMessage());
            }
        });

        // POST /clientes — criar
        post("/clientes", (req, res) -> {
            res.type("application/json");
            try {
                Cliente c = gson.fromJson(req.body(), Cliente.class);
                if (c.getDataAdicao() == null)
                    c.setDataAdicao(LocalDate.now());
                boolean ok = clienteDAO.incluirCliente(c);
                if (ok) {
                    res.status(201);
                    return ok("Cliente criado com sucesso");
                }
                res.status(500);
                return erro("Falha ao criar cliente");
            } catch (Exception e) {
                res.status(400);
                return erro(e.getMessage());
            }
        });

        // PUT /clientes/:id — atualizar
        put("/clientes/:id", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params(":id"));
                Cliente c = gson.fromJson(req.body(), Cliente.class);
                c.setId(id);
                boolean ok = clienteDAO.alterarCliente(c);
                if (ok)
                    return ok("Cliente atualizado");
                res.status(404);
                return erro("Cliente não encontrado");
            } catch (Exception e) {
                res.status(400);
                return erro(e.getMessage());
            }
        });

        // DELETE /clientes/:id — excluir
        delete("/clientes/:id", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params(":id"));
                boolean ok = clienteDAO.excluirCliente(id);
                if (ok)
                    return ok("Cliente excluído");
                res.status(404);
                return erro("Cliente não encontrado");
            } catch (Exception e) {
                res.status(500);
                return erro(e.getMessage());
            }
        });

        // POST /clientes/login — autenticação simples
        post("/clientes/login", (req, res) -> {
            res.type("application/json");
            try {
                LoginRequest lr = gson.fromJson(req.body(), LoginRequest.class);
                Cliente[] todos = clienteDAO.listarClientes();
                for (Cliente c : todos) {
                    // Verifica e-mail e senha (XOR quando implementado no futuro)
                    boolean emailOk = false;
                    for (String e : c.getEmail()) {
                        if (e.equalsIgnoreCase(lr.email)) {
                            emailOk = true;
                            break;
                        }
                    }
                    if (emailOk && c.getSenha().equals(lr.senha)) {
                        // Não retorna a senha
                        c.setSenha(null);
                        return gson.toJson(c);
                    }
                }
                res.status(401);
                return erro("Credenciais inválidas");
            } catch (Exception e) {
                res.status(400);
                return erro(e.getMessage());
            }
        });

        // POST /clientes/esqueci-senha/solicitar — gera código de recuperação
        post("/clientes/esqueci-senha/solicitar", (req, res) -> {
            res.type("application/json");
            try {
                ResetCodeRequest rc = gson.fromJson(req.body(), ResetCodeRequest.class);
                if (rc == null || rc.email == null || rc.email.isBlank()) {
                    res.status(400);
                    return erro("E-mail é obrigatório");
                }

                Cliente[] todos = clienteDAO.listarClientes();
                for (Cliente c : todos) {
                    boolean emailOk = false;
                    for (String e : c.getEmail()) {
                        if (e.equalsIgnoreCase(rc.email)) {
                            emailOk = true;
                            break;
                        }
                    }

                    if (emailOk) {
                        String emailKey = rc.email.trim().toLowerCase();
                        String codigo = String.format("%06d", random.nextInt(1_000_000));
                        long expiraEm = System.currentTimeMillis() + RESET_TOKEN_TTL_MS;
                        resetTokens.put(emailKey, new ResetToken(codigo, expiraEm));

                        // Simulação de envio por e-mail (console do servidor)
                        System.out.println("[RECUPERACAO] Codigo para " + rc.email + ": " + codigo);
                        return ok("Código enviado para o e-mail informado");
                    }
                }

                res.status(404);
                return erro("E-mail não encontrado");
            } catch (Exception e) {
                res.status(400);
                return erro(e.getMessage());
            }
        });

        // POST /clientes/esqueci-senha/confirmar — valida código e altera senha
        post("/clientes/esqueci-senha/confirmar", (req, res) -> {
            res.type("application/json");
            try {
                ForgotPasswordConfirmRequest fr = gson.fromJson(req.body(), ForgotPasswordConfirmRequest.class);
                if (fr == null || fr.email == null || fr.email.isBlank()
                        || fr.codigo == null || fr.codigo.isBlank()
                        || fr.novaSenha == null || fr.novaSenha.isBlank()) {
                    res.status(400);
                    return erro("E-mail, código e nova senha são obrigatórios");
                }

                String emailKey = fr.email.trim().toLowerCase();
                ResetToken token = resetTokens.get(emailKey);
                if (token == null) {
                    res.status(400);
                    return erro("Solicite um novo código de recuperação");
                }

                if (System.currentTimeMillis() > token.expiresAt) {
                    resetTokens.remove(emailKey);
                    res.status(400);
                    return erro("Código expirado. Solicite um novo código");
                }

                if (!token.code.equals(fr.codigo.trim())) {
                    res.status(400);
                    return erro("Código inválido");
                }

                Cliente[] todos = clienteDAO.listarClientes();
                for (Cliente c : todos) {
                    boolean emailOk = false;
                    for (String e : c.getEmail()) {
                        if (e.equalsIgnoreCase(fr.email)) {
                            emailOk = true;
                            break;
                        }
                    }

                    if (emailOk) {
                        c.setSenha(fr.novaSenha);
                        boolean ok = clienteDAO.alterarCliente(c);
                        if (ok) {
                            resetTokens.remove(emailKey);
                            return ok("Senha atualizada com sucesso");
                        }

                        res.status(500);
                        return erro("Falha ao atualizar senha");
                    }
                }

                res.status(404);
                return erro("E-mail não encontrado");
            } catch (Exception e) {
                res.status(400);
                return erro(e.getMessage());
            }
        });

        // ══════════════════════════════════════════════════════════════════
        // ALIMENTOS
        // ══════════════════════════════════════════════════════════════════

        get("/alimentos", (req, res) -> {
            res.type("application/json");
            try {
                return gson.toJson(alimentoDAO.listarAlimentos());
            } catch (Exception e) {
                res.status(500);
                return erro(e.getMessage());
            }
        });

        put("/alimentos/:id", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params(":id"));
                Alimento a = gson.fromJson(req.body(), Alimento.class);
                a.setId(id);
                if (a.getCategoria() == null)
                    a.setCategoria(new String[0]);
                boolean ok = alimentoDAO.alterarAlimento(a);
                if (ok)
                    return ok("Alimento atualizado");
                res.status(404);
                return erro("Alimento não encontrado");
            } catch (Exception e) {
                res.status(400);
                return erro(e.getMessage());
            }
        });

        // GET /alimentos/busca?q=termo — busca por nome
        get("/alimentos/busca", (req, res) -> {
            res.type("application/json");
            try {
                String q = req.queryParams("q");
                if (q == null || q.isBlank()) {
                    return gson.toJson(alimentoDAO.listarAlimentos());
                }
                Alimento a = alimentoDAO.buscarAlimentoNome(q);
                return gson.toJson(a != null ? new Alimento[] { a } : new Alimento[0]);
            } catch (Exception e) {
                res.status(500);
                return erro(e.getMessage());
            }
        });

        // GET /alimentos/categoria/:categoria
        get("/alimentos/categoria/:categoria", (req, res) -> {
            res.type("application/json");
            try {
                String categoria = req.params(":categoria");
                ArrayList<Alimento> lista = alimentoDAO.buscarAlimentoCategoria(categoria);
                return gson.toJson(lista);
            } catch (Exception e) {
                res.status(500);
                return erro(e.getMessage());
            }
        });

        post("/alimentos", (req, res) -> {
            res.type("application/json");
            try {
                Alimento a = gson.fromJson(req.body(), Alimento.class);
                if (a.getCategoria() == null)
                    a.setCategoria(new String[0]);
                boolean ok = alimentoDAO.incluirAlimento(a);
                if (ok) {
                    res.status(201);
                    return ok("Alimento criado");
                }
                res.status(500);
                return erro("Falha ao criar alimento");
            } catch (Exception e) {
                res.status(400);
                return erro(e.getMessage());
            }
        });

        put("/alimentos/:id", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params(":id"));
                Alimento a = gson.fromJson(req.body(), Alimento.class);
                a.setId(id);
                boolean ok = alimentoDAO.alterarAlimento(a);
                if (ok)
                    return ok("Alimento atualizado");
                res.status(404);
                return erro("Alimento não encontrado");
            } catch (Exception e) {
                res.status(400);
                return erro(e.getMessage());
            }
        });

        delete("/alimentos/:id", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params(":id"));
                boolean ok = alimentoDAO.excluirAlimento(id);
                if (ok)
                    return ok("Alimento excluído");
                res.status(404);
                return erro("Alimento não encontrado");
            } catch (Exception e) {
                res.status(500);
                return erro(e.getMessage());
            }
        });

        // ══════════════════════════════════════════════════════════════════
        // RECEITAS
        // ══════════════════════════════════════════════════════════════════

        get("/receitas", (req, res) -> {
            res.type("application/json");
            try {
                return gson.toJson(receitaDAO.listarReceitas());
            } catch (Exception e) {
                res.status(500);
                return erro(e.getMessage());
            }
        });

        get("/receitas/:id", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params(":id"));
                Receita r = receitaDAO.buscarReceitaID(id);
                if (r != null)
                    return gson.toJson(r);
                res.status(404);
                return erro("Receita não encontrada");
            } catch (Exception e) {
                res.status(500);
                return erro(e.getMessage());
            }
        });

        // GET /receitas/busca?q=titulo
        get("/receitas/busca", (req, res) -> {
            res.type("application/json");
            try {
                String q = req.queryParams("q");
                if (q == null || q.isBlank()) {
                    return gson.toJson(receitaDAO.listarReceitas());
                }
                Receita r = receitaDAO.buscarReceitaTitulo(q);
                return gson.toJson(r != null ? new Receita[] { r } : new Receita[0]);
            } catch (Exception e) {
                res.status(500);
                return erro(e.getMessage());
            }
        });

        // GET /receitas/tempo/:tempo
        get("/receitas/tempo/:tempo", (req, res) -> {
            res.type("application/json");
            try {
                int tempo = Integer.parseInt(req.params(":tempo"));
                Receita r = receitaDAO.buscarReceitaTempo(tempo);
                return gson.toJson(r != null ? new Receita[] { r } : new Receita[0]);
            } catch (Exception e) {
                res.status(500);
                return erro(e.getMessage());
            }
        });

        post("/receitas", (req, res) -> {
            res.type("application/json");
            try {
                Receita r = gson.fromJson(req.body(), Receita.class);
                boolean ok = receitaDAO.incluirReceita(r);
                if (ok) {
                    res.status(201);
                    return ok("Receita criada");
                }
                res.status(500);
                return erro("Falha ao criar receita");
            } catch (Exception e) {
                res.status(400);
                return erro(e.getMessage());
            }
        });

        put("/receitas/:id", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params(":id"));
                Receita r = gson.fromJson(req.body(), Receita.class);
                r.setId(id);
                boolean ok = receitaDAO.alterarReceita(r);
                if (ok)
                    return ok("Receita atualizada");
                res.status(404);
                return erro("Receita não encontrada");
            } catch (Exception e) {
                res.status(400);
                return erro(e.getMessage());
            }
        });

        delete("/receitas/:id", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params(":id"));
                boolean ok = receitaDAO.excluirReceita(id);
                if (ok)
                    return ok("Receita excluída");
                res.status(404);
                return erro("Receita não encontrada");
            } catch (Exception e) {
                res.status(500);
                return erro(e.getMessage());
            }
        });

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   NutriChef API — porta " + PORTA + "        ║");
        System.out.println("║   http://localhost:" + PORTA + "             ║");
        System.out.println("╚══════════════════════════════════════╝");
    }

    // ── Helpers ──────────────────────────────────────────────────────────
    private static String erro(String msg) {
        return "{\"erro\":\"" + msg.replace("\"", "'") + "\"}";
    }

    private static String ok(String msg) {
        return "{\"mensagem\":\"" + msg + "\"}";
    }

    static class LoginRequest {
        String email;
        String senha;
    }

    static class ResetCodeRequest {
        String email;
    }

    static class ForgotPasswordConfirmRequest {
        String email;
        String codigo;
        String novaSenha;
    }

    static class ResetToken {
        String code;
        long expiresAt;

        ResetToken(String code, long expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }
}
