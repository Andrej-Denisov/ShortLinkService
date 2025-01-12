import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

public class LinkService {
    private static final String BASE_62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final Map<String, Link> links = new HashMap<>();
    private final Properties config = new Properties();

    public LinkService() {
        loadConfig();
    }

    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Unable to find config.properties");
                return;
            }
            config.load(input);
        } catch (IOException ex) {
            System.out.println("Error loading config file: " + ex.getMessage());
        }
    }


    public String createShortLink(String longUrl, Integer userMaxClicks, Integer userLifetimeHours, UUID userId) {
        int defaultMaxClicks = Integer.parseInt(config.getProperty("defaultMaxClicks", "3"));
        int defaultLifetimeHours = Integer.parseInt(config.getProperty("defaultLinkLifetimeHours", "24"));

        int maxClicks = userMaxClicks != null ? Math.min(userMaxClicks, defaultMaxClicks) : defaultMaxClicks;
        int lifetimeHours = userLifetimeHours != null ? Math.min(userLifetimeHours, defaultLifetimeHours) : defaultLifetimeHours;

        String shortUrl;
        do {
            shortUrl = generateShortUrl();
        } while (links.containsKey(shortUrl));

        Link link = new Link(shortUrl, longUrl, maxClicks, lifetimeHours, userId);
        links.put(shortUrl, link);
        return shortUrl;
    }
    private String generateShortUrl() {
        SecureRandom random = new SecureRandom();
        StringBuilder shortUrl = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            shortUrl.append(BASE_62_CHARS.charAt(random.nextInt(BASE_62_CHARS.length())));
        }
        return shortUrl.toString();
    }


    public String getLongUrl(String shortUrl) {
        Link link = links.get(shortUrl);
        if (link == null) {
            return null;
        }

        if (link.isExpired()) {
            links.remove(shortUrl);
            return "Ссылка устарела.";
        }

        if(link.isLimitExceeded()){
            return "Превышен лимит переходов.";
        }

        link.incrementClicks();
        return link.getLongUrl();
    }

    public void removeExpiredLinks() {
        links.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public String deleteLink(String shortUrl, UUID userId){
        Link link = links.get(shortUrl);

        if(link == null){
            return "Ссылка не найдена";
        }

        if(!link.isOwnedByUser(userId)){
            return "У вас нет прав на удаление данной ссылки";
        }

        links.remove(shortUrl);
        return "Ссылка удалена";
    }

    public String editMaxClicks(String shortUrl, int maxClicks, UUID userId){
        Link link = links.get(shortUrl);
        if(link == null){
            return "Ссылка не найдена";
        }
        if(!link.isOwnedByUser(userId)){
            return "У вас нет прав на редактирование данной ссылки";
        }
        link.setMaxClicks(maxClicks);
        return "Лимит переходов изменен на " + maxClicks;
    }

    public Map<String, Link> getAllLinksForUser(UUID userId){
        return links.entrySet().stream()
                .filter(entry -> entry.getValue().isOwnedByUser(userId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}