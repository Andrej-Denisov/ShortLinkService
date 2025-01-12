import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;

public class Main {
    private static final LinkService linkService = new LinkService();
    private static final UserService userService = new UserService();
    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private static UUID currentUserUUID;
    private static String currentUserIdentifier = "ConsoleUser";

    public static void main(String[] args) throws IOException {
        currentUserUUID = userService.getOrCreateUserUUID(currentUserIdentifier);
        System.out.println("ДЗ А.В.Денисов - Сервис коротких ссылок");
        while (true) {
            System.out.println("\nВыберите действие:");
            System.out.println("1. Создать короткую ссылку");
            System.out.println("2. Перейти по короткой ссылке");
            System.out.println("3. Удалить ссылку");
            System.out.println("4. Мои ссылки");
            System.out.println("5. Редактировать лимит переходов");
            System.out.println("0. Выход");
            System.out.print("Введите номер выбранного действия: ");

            String choice = reader.readLine();
            try {
                switch (choice) {
                    case "1":
                        createLink();
                        break;
                    case "2":
                        redirectToLongUrl();
                        break;
                    case "3":
                        deleteLink();
                        break;
                    case "4":
                        myLinks();
                        break;
                    case "5":
                        editMaxClicks();
                        break;
                    case "0":
                        System.out.println("Выход из программы.");
                        return;
                    default:
                        System.out.println("Неверный выбор. Пожалуйста, попробуйте еще раз.");
                }
            } catch (IOException | URISyntaxException e) {
                System.out.println("Произошла ошибка: " + e.getMessage());
            }
            linkService.removeExpiredLinks();
        }
    }

    private static void createLink() throws IOException {
        System.out.println("Введите длинную ссылку:");
        String longUrl = reader.readLine();

        System.out.println("Введите лимит переходов (или оставьте пустым):");
        String maxClicksInput = reader.readLine();
        Integer maxClicks = maxClicksInput.isEmpty() ? null : Integer.parseInt(maxClicksInput);

        System.out.println("Введите время жизни ссылки в часах (или оставьте пустым):");
        String lifetimeHoursInput = reader.readLine();
        Integer lifetimeHours = lifetimeHoursInput.isEmpty() ? null : Integer.parseInt(lifetimeHoursInput);

        String shortUrl = linkService.createShortLink(longUrl, maxClicks, lifetimeHours, currentUserUUID);
        System.out.println("Короткая ссылка: " + shortUrl);
    }

    private static void redirectToLongUrl() throws IOException, URISyntaxException {
        System.out.println("Введите короткую ссылку:");
        String shortUrl = reader.readLine();
        String longUrl = linkService.getLongUrl(shortUrl);
        if (longUrl == null) {
            System.out.println("Ссылка не найдена.");
        } else if (longUrl == "Ссылка устарела.") {
            System.out.println("Ссылка устарела.");
        } else if (longUrl == "Превышен лимит переходов.") {
            System.out.println("Превышен лимит переходов.");
        } else {
            Desktop.getDesktop().browse(new URI(longUrl));
        }
    }

    private static void deleteLink() throws IOException{
        System.out.println("Введите короткую ссылку для удаления");
        String shortUrl = reader.readLine();
        String result = linkService.deleteLink(shortUrl, currentUserUUID);
        System.out.println(result);
    }

    private static void editMaxClicks() throws IOException{
        System.out.println("Введите короткую ссылку для редактирования лимита переходов");
        String shortUrl = reader.readLine();
        System.out.println("Введите новый лимит переходов");
        int maxClicks = Integer.parseInt(reader.readLine());
        String result = linkService.editMaxClicks(shortUrl, maxClicks, currentUserUUID);
        System.out.println(result);
    }

    private static void myLinks() {
        Map<String, Link> userLinks = linkService.getAllLinksForUser(currentUserUUID);

        if(userLinks.isEmpty()){
            System.out.println("У вас нет ссылок");
        }else {
            System.out.println("Ваши ссылки: ");
            userLinks.forEach((key, value) -> {
                System.out.println("Короткая ссылка: " + key + ", Длинная ссылка: " + value.getLongUrl() + ", лимит: " + value.getMaxClicks() + ", переходов: " + value.getClicks() + ", срок действия: " + value.getExpirationTime() );
            });
        }
    }
}