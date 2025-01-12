import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserService {
    private final Map<String, UUID> userMap = new HashMap<>();

    public UUID getOrCreateUserUUID(String userIdentifier) {
        return userMap.computeIfAbsent(userIdentifier, key -> UUID.randomUUID());
    }
}