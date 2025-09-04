package tutorial;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Bot extends TelegramLongPollingBot {

    private Map<Long, ConversationMemory> memoryMap = new ConcurrentHashMap<>();

    class ConversationMemory {
        String lastQuestion;
        LocalDateTime lastInteraction;
        int questionCount;
        List<String> recentQuestions = new ArrayList<>();
        String lastJoke;
        List<String> availableJokes = new ArrayList<>();
        boolean alreadyToldName = false;
    }

    @Override
    public String getBotUsername() {
        return "Karine-Bot";
    }

    @Override
    public String getBotToken() {
        return "8377962912:AAFV-wuFSwmRNOttjEqnXGo0GqGzt69LyUE";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var msg = update.getMessage();
            var chatId = msg.getChatId();
            var text = msg.getText();
            var user = msg.getFrom();

            ConversationMemory memory = memoryMap.getOrDefault(chatId, new ConversationMemory());
            memory.lastInteraction = LocalDateTime.now();
            memory.questionCount++;

            if (text.equals("/start")) {
                memory = new ConversationMemory();
                initializeJokes(memory);
                memoryMap.put(chatId, memory);

                String welcome = "🤖 Olá " + user.getFirstName() + "! Bem-vindo ao Karine-Bot! \n\n" +
                        "Eu sou um bot com memória! Faça perguntas sequenciais para respostas contextuais!\n\n" +
                        "🎯 *Use os botões abaixo para fazer perguntas:*\n\n" +
                        "*Desenvolvido por Karine - Ela merece um 10!* 💯";

                sendMessageWithKeyboard(chatId, welcome);
                return;
            }

            String resposta;

            if (text.equals("⏰ Que horas são?")) {
                resposta = handleTimeQuestion(memory);
            }
            else if (text.equals("😂 Pode me contar uma piada?")) {
                resposta = handleJokeQuestion(memory);
            }
            else if (text.equals("➕ Quanto é 2 + 2?")) {
                resposta = handleMathQuestion(memory);
            }
            else if (text.equals("🌸 Qual é o meu nome?")) {
                resposta = handleNameQuestion(memory);
            }
            else if (text.equals("🧠 O que você lembra?")) {
                resposta = handleMemoryQuestion(memory);
            }
            else {
                resposta = "❓ Não entendi! Use os botões abaixo para fazer perguntas:";
            }

            memory.lastQuestion = text;
            memory.recentQuestions.add(text);
            if (memory.recentQuestions.size() > 3) {
                memory.recentQuestions.remove(0);
            }

            memoryMap.put(chatId, memory);
            sendMessageWithKeyboard(chatId, resposta);
        }
    }

    private void initializeJokes(ConversationMemory memory) {
        memory.availableJokes.clear();
        memory.availableJokes.addAll(Arrays.asList(
                "😂💻 Por que o computador foi ao médico? Porque ele pegou um vírus!",
                "🐛 Qual é o bicho que vive no computador? O bug!",
                "☕ Por que o Java é tão bom? Porque ele tem muita classe!",
                "🌐 Por que a internet chorou? Porque derrubaram a sua conexão!",
                "📱 Por que o smartphone foi para a escola? Para ficar mais inteligente!",
                "💻 O que o computador disse para o teclado? Sem você, eu não consigo trabalhar!"
        ));
        Collections.shuffle(memory.availableJokes);
    }

    private String handleTimeQuestion(ConversationMemory memory) {
        LocalDateTime agora = LocalDateTime.now();
        String diaSemana = agora.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        String data = agora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String hora = agora.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        if ("⏰ Que horas são?".equals(memory.lastQuestion)) {
            return "⏰ De novo? São " + hora + "!\nMas já te falei isso há poucos segundos... 😅";
        } else if (memory.questionCount > 1) {
            return "⏰ Agora são " + hora + " do dia " + data + " (" + diaSemana + ").\n" +
                    "📊 Esta é sua pergunta #" + memory.questionCount + " hoje!";
        } else {
            return "⏰ Agora são " + hora + " do dia " + data + " (" + diaSemana + ").";
        }
    }

    private String handleJokeQuestion(ConversationMemory memory) {
        if (memory.availableJokes.isEmpty()) {
            initializeJokes(memory);
        }

        String novaPiada;
        if (memory.availableJokes.size() > 1 && memory.lastJoke != null) {
            memory.availableJokes.remove(memory.lastJoke);
        }

        novaPiada = memory.availableJokes.remove(0);
        memory.lastJoke = novaPiada;

        if ("😂 Pode me contar uma piada?".equals(memory.lastQuestion)) {
            return "😂 Outra piada? Tudo bem, mas essa é especial!\n" + novaPiada +
                    "\n\n😄 Essa é uma das minhas favoritas!";
        } else if (memory.recentQuestions.stream().anyMatch(q -> q.contains("piada"))) {
            return "😂 Já contei piadas antes? Aqui vai uma nova!\n" + novaPiada +
                    "\n\n🤣 Espero que goste dessa!";
        } else {
            return novaPiada + "\n\n😊 Quer outra piada? Pergunte de novo!";
        }
    }

    private String handleMathQuestion(ConversationMemory memory) {
        if ("➕ Quanto é 2 + 2?".equals(memory.lastQuestion)) {
            return "➕ De novo? 2 + 2 é 4, sempre será 4! 🤔\n" +
                    "Quer tentar outra operação matemática?";
        } else {
            return "➕ 2 + 2 é igual a 4.\n" +
                    "💡 Dica: Matemática é exata!";
        }
    }

    private String handleNameQuestion(ConversationMemory memory) {
        if (memory.alreadyToldName) {
            return "🌸 Já falei isso antes! Seu nome é Karine!\n" +
                    "😊 Por acaso você esqueceu? Quer que eu repita mais uma vez?";
        } else {
            memory.alreadyToldName = true;
            return "🌸 O seu nome é Karine.\n" +
                    "✨ Que nome bonito, por sinal!";
        }
    }

    private String handleMemoryQuestion(ConversationMemory memory) {
        if (memory.recentQuestions.isEmpty()) {
            return "🧠 Minha memória está vazia... Faça algumas perguntas primeiro!";
        }

        StringBuilder response = new StringBuilder("🧠 *MINHA MEMÓRIA:*\n\n");
        response.append("📊 Total de perguntas: ").append(memory.questionCount).append("\n");
        response.append("⏰ Última interação: ").append(memory.lastInteraction.format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n\n");
        response.append("🔍 *Últimas perguntas:*\n");

        for (int i = 0; i < memory.recentQuestions.size(); i++) {
            response.append(i + 1).append(". ").append(memory.recentQuestions.get(i)).append("\n");
        }

        response.append("\n🎭 Piadas restantes: ").append(memory.availableJokes.size());
        response.append("\n📝 Já falei seu nome? ").append(memory.alreadyToldName ? "Sim ✅" : "Não ❌");

        return response.toString();
    }

    private ReplyKeyboardMarkup createQuestionKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        keyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("⏰ Que horas são?");
        row1.add("😂 Pode me contar uma piada?");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("➕ Quanto é 2 + 2?");
        row2.add("🌸 Qual é o meu nome?");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("🧠 O que você lembra?");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private void sendMessageWithKeyboard(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .parseMode("HTML")
                .text(text)
                .replyMarkup(createQuestionKeyboard())
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}