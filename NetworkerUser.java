

import java.util.List;

public interface NetworkerUser {
    void onRecieve(String string, Networker networker);
    void onTearedOff(String id ,List<String> unsentMsgs, Networker networker);
}