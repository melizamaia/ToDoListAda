import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    static class Task {
        private final long id;
        private String title;
        private String description;
        private LocalDate dueDate;
        private boolean completed;

        Task(long id, String title, String description, LocalDate dueDate) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.dueDate = dueDate;
            this.completed = false;
        }

        public long getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public LocalDate getDueDate() { return dueDate; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }

        @Override
        public String toString() {
            return String.format(
                    "#%d | %s | %s | entrega: %s | %s",
                    id,
                    title,
                    (description == null || description.isBlank()) ? "(sem descrição)" : description,
                    (dueDate != null ? dueDate : "—"),
                    (completed ? "✅ concluída" : "⏳ pendente")
            );
        }
    }

    static class TaskRepo {
        private final Map<Long, Task> data = new LinkedHashMap<>();
        private final AtomicLong seq = new AtomicLong(1);

        public Task create(String title, String description, LocalDate dueDate) {
            long id = seq.getAndIncrement();
            Task t = new Task(id, title, description, dueDate);
            data.put(id, t);
            return t;
        }

        public List<Task> listPending() {
            return data.values().stream()
                    .filter(t -> !t.isCompleted())
                    .sorted(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
        }

        public List<Task> searchByTitle(String q) {
            String needle = q.toLowerCase(Locale.ROOT);
            return data.values().stream()
                    .filter(t -> t.getTitle().toLowerCase(Locale.ROOT).contains(needle))
                    .toList();
        }

        public Optional<Task> findById(long id) {
            return Optional.ofNullable(data.get(id));
        }

        public boolean delete(long id) {
            return data.remove(id) != null;
        }

        public List<Task> listAll() {
            return new ArrayList<>(data.values());
        }
    }

    private static final Scanner SC = new Scanner(System.in);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) {
        TaskRepo repo = new TaskRepo();

        while (true) {
            System.out.println("\n=== ToDo List ===");
            System.out.println("1) Criar nova tarefa");
            System.out.println("2) Listar tarefas pendentes");
            System.out.println("3) Buscar tarefa por título");
            System.out.println("4) Marcar tarefa como concluída");
            System.out.println("5) Excluir tarefa");
            System.out.println("6) Listar todas");
            System.out.println("0) Sair");
            System.out.print("Escolha: ");

            String op = SC.nextLine().trim();

            try {
                switch (op) {
                    case "1" -> create(repo);
                    case "2" -> listPending(repo);
                    case "3" -> search(repo);
                    case "4" -> complete(repo);
                    case "5" -> delete(repo);
                    case "6" -> listAll(repo);
                    case "0" -> { System.out.println("Até mais!"); return; }
                    default -> System.out.println("Opção inválida.");
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    private static void create(TaskRepo repo) {
        System.out.print("Título (obrigatório): ");
        String title = SC.nextLine().trim();
        if (title.isBlank()) {
            System.out.println("Título não pode ser vazio.");
            return;
        }

        System.out.print("Descrição (opcional): ");
        String desc = SC.nextLine().trim();

        System.out.print("Data de entrega (dd/MM/yyyy) ou vazio: ");
        String dueRaw = SC.nextLine().trim();
        LocalDate due = null;
        if (!dueRaw.isBlank()) {
            try {
                due = LocalDate.parse(dueRaw, FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida.");
                return;
            }
        }

        Task t = repo.create(title, desc, due);
        System.out.println("Criada: " + t);
    }

    private static void listPending(TaskRepo repo) {
        var list = repo.listPending();
        if (list.isEmpty()) {
            System.out.println("Sem tarefas pendentes.");
            return;
        }
        list.forEach(System.out::println);
    }

    private static void search(TaskRepo repo) {
        System.out.print("Título contém: ");
        String q = SC.nextLine().trim();
        var list = repo.searchByTitle(q);
        if (list.isEmpty()) {
            System.out.println("Nada encontrado.");
            return;
        }
        list.forEach(System.out::println);
    }

    private static void complete(TaskRepo repo) {
        System.out.print("ID da tarefa: ");
        long id = Long.parseLong(SC.nextLine());
        var t = repo.findById(id).orElse(null);
        if (t == null) {
            System.out.println("Não encontrada.");
            return;
        }
        t.setCompleted(true);
        System.out.println("Concluída: " + t);
    }

    private static void delete(TaskRepo repo) {
        System.out.print("ID da tarefa: ");
        long id = Long.parseLong(SC.nextLine());
        boolean ok = repo.delete(id);
        System.out.println(ok ? "Excluída." : "Não encontrada.");
    }

    private static void listAll(TaskRepo repo) {
        var list = repo.listAll();
        if (list.isEmpty()) {
            System.out.println("Sem tarefas.");
            return;
        }
        list.forEach(System.out::println);
    }
}