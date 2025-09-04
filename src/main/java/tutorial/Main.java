package tutorial;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        Bot bot = new Bot();
        botsApi.registerBot(bot);

        System.out.println("✅ Bot Karine-Bot iniciado com sucesso!");
        System.out.println("📱 Agora ele está ouvindo mensagens do Telegram...");
        System.out.println("💬 Envie /start para seu bot no Telegram para testar!");
    }
}