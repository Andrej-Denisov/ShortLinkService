import java.time.LocalDateTime;
import java.util.UUID;

public class Link {
    private String shortUrl;
    private String longUrl;
    private int maxClicks;
    private int clicks;
    private LocalDateTime creationTime;
    private LocalDateTime expirationTime;
    private UUID userId;

    public Link(String shortUrl, String longUrl, int maxClicks, int lifetimeHours, UUID userId) {
        this.shortUrl = shortUrl;
        this.longUrl = longUrl;
        this.maxClicks = maxClicks;
        this.clicks = 0;
        this.creationTime = LocalDateTime.now();
        this.expirationTime = creationTime.plusHours(lifetimeHours);
        this.userId = userId;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public int getMaxClicks() {
        return maxClicks;
    }

    public int getClicks() {
        return clicks;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public LocalDateTime getExpirationTime() { return expirationTime;}

    public UUID getUserId() {
        return userId;
    }

    public void incrementClicks() {
        this.clicks++;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }

    public boolean isLimitExceeded() {
        return clicks >= maxClicks;
    }

    public boolean isOwnedByUser(UUID userId){
        return this.userId.equals(userId);
    }

    public void setMaxClicks(int maxClicks){
        this.maxClicks = maxClicks;
    }

}