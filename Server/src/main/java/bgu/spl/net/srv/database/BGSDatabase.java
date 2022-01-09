package bgu.spl.net.srv.database;

import bgu.spl.net.srv.connections.Connections;
import bgu.spl.net.srv.connections.ConnectionsImpl;
import bgu.spl.net.srv.msg.MessageTuple;
import bgu.spl.net.srv.msg.NOTIFICATION;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class BGSDatabase {

    private ConnectionsImpl dbConnections;
    private HashMap<String, User> userMap;
    private HashMap<Integer, User> userMapByConn;
    private LinkedList<MessageTuple> messageLog;

    private BGSDatabase(){
        dbConnections = new ConnectionsImpl();
        userMap = new HashMap<>();
        userMapByConn = new HashMap<>();
        messageLog = new LinkedList<>();

    }
    private static class SingletonClassHolder {
        static final BGSDatabase instance = new BGSDatabase();
    }
    public static BGSDatabase getInstance() {
        return SingletonClassHolder.instance;
    }

    public HashMap<String, User> getUserMap() {
        return userMap;
    }

    public HashMap<Integer, User> getUserMapByConn() {
        return userMapByConn;
    }

    public boolean register(String name, String password, String birthday, int connectionID ){
        if(userMap.containsKey(name))
            return false;
        User currentUser = new User(name, password, birthday);
        userMap.putIfAbsent(name, currentUser);
        currentUser.setConnectionHandlerID(connectionID);
        return true;
    }

    public <T> boolean logIn(int connectionID, Connections<T> connections, String username, String password){
        if(!userMap.containsKey(username) || !password.equals(userMap.get(username).getPassword())
        || userMap.get(username).isloggedIn())
            return false;
        if(userMapByConn.containsKey(connectionID))  // TRY: FORBIDDING MULTIPLE LOGINS
            return false;
        userMapByConn.putIfAbsent(connectionID, userMap.get(username));
        userMap.get(username).login();
        //receive missed messages
        if(!userMap.get(username).getAwaitingMessages().isEmpty()) {
            for (NOTIFICATION message : userMap.get(username).getAwaitingMessages())
                connections.send(connectionID, (T) message);
            userMap.get(username).getAwaitingMessages().clear();
        }
        return true;
    }

    public boolean logOut(int connectionsId){
        if(!userMapByConn.containsKey(connectionsId) || !userMapByConn.get(connectionsId).isloggedIn())
            return false;
        userMapByConn.get(connectionsId).logout();
        dbConnections.disconnect(connectionsId);
        userMapByConn.remove(connectionsId);
        return true;
    }

    public boolean follow(int connectionsId, String userToFollow){
        if(!userMapByConn.containsKey(connectionsId) || !userMap.containsKey(userToFollow) )
            return false;
        User currentUser = userMapByConn.get(connectionsId);
        User toFollow = userMap.get(userToFollow);
        if(currentUser == toFollow) // Cannot follow yourself
            return false;
        if(currentUser.getFollowing().contains(toFollow) || !currentUser.isloggedIn()
        || currentUser.isBlockingUser(userMap.get(userToFollow))|| toFollow.isBlockingUser(currentUser))
            return false;
        currentUser.unfollowOrFollow(userMap.get(userToFollow), true);
        return true;
    }

    public boolean unfollow(int connectionsId, String userToUnfollow){
        if(!userMapByConn.containsKey(connectionsId) ||!userMap.containsKey(userToUnfollow) )
            return false;
        User currentUser = userMapByConn.get(connectionsId);
        User toUnfollow= userMap.get(userToUnfollow);
        if(currentUser == toUnfollow)
            return false;
        if( !currentUser.getFollowing().contains(toUnfollow) || !currentUser.isloggedIn())
            return false;
        currentUser.unfollowOrFollow(userMap.get(userToUnfollow), false);
        return true;
    }

    public <T> boolean post(int connectionsId, Connections<T> connections, List<String> taggedUsers, String msg){
        if(!userMapByConn.containsKey(connectionsId) || !userMapByConn.get(connectionsId).isloggedIn())
            return false;
        User currentUser = userMapByConn.get(connectionsId);
        LinkedList<User> recipientsForTuple = new LinkedList<>();
        for(User follower : userMapByConn.get(connectionsId).getFollowers()){
            recipientsForTuple.add(follower);
            if(follower.isloggedIn()) {
                connections.send(follower.getConnectionHandlerID(), (T) new NOTIFICATION((byte) 1, currentUser.getUsername(), msg));
            }else{
                follower.getAwaitingMessages().add(new NOTIFICATION( (byte) 1, currentUser.getUsername(), msg));
            }
        }
        for (String taggedUser : taggedUsers){
            User tagged = userMap.get(taggedUser); //message already sent
            if(tagged.isBlockingUser(currentUser))
                continue;
            if(!currentUser.getFollowers().contains(tagged) ){
                recipientsForTuple.add(tagged);
                if(tagged.isloggedIn()) {
                    connections.send(tagged.getConnectionHandlerID(), (T) new NOTIFICATION((byte) 1, currentUser.getUsername(), msg));
                }else{
                    tagged.getAwaitingMessages().add(new NOTIFICATION( (byte) 1, currentUser.getUsername(), msg));
                }
            }
        }
        currentUser.upNumPosts();
        //log messages
        messageLog.add(new MessageTuple(currentUser, recipientsForTuple, msg, "", NOTIFICATION.Type.Post));
        return true;
    }

    public <T> boolean sendPM(int connectionsId, Connections<T> connections, String msg, String date, String userToReceive ){
        if(!userMapByConn.containsKey(connectionsId) || !userMap.containsKey(userToReceive))
            return false;
        User currentUser = userMapByConn.get(connectionsId);
        User receivingUser = userMap.get(userToReceive);
        if(!currentUser.getFollowing().contains(receivingUser) || receivingUser.isBlockingUser(currentUser))
            return false;
        if(receivingUser.isloggedIn())
            connections.send(receivingUser.getConnectionHandlerID(), (T) new NOTIFICATION((byte) 0, currentUser.getUsername(), msg));
        else{
            receivingUser.getAwaitingMessages().add(new NOTIFICATION((byte) 0, currentUser.getUsername(), msg));
        }
        //log messages
        LinkedList<User> recipients = new LinkedList<>();
        recipients.add(receivingUser);
        messageLog.add(new MessageTuple(currentUser, recipients , msg, date, NOTIFICATION.Type.PM));
        return true;
    }

    public HashMap<User, String> logStat(int connectionsId){
        HashMap<User, String> output = new HashMap<>();
        if(!userMapByConn.containsKey(connectionsId) || !userMapByConn.get(connectionsId).isloggedIn())
            return null;
        User current = userMapByConn.get(connectionsId);
        for(User user: userMap.values()) {
            if (user.isloggedIn() && !checkBlock(current, user) && !checkBlock(user, current))
                output.put(user, user.getUsername());
        }
        return output;
    }

    public LinkedList<String> stat(List<String> usersList, int connectionsId){
        if(!userMapByConn.containsKey(connectionsId) || !userMapByConn.get(connectionsId).isloggedIn())
            return null;
        LinkedList<String> outputLog = new LinkedList<>();
        User current = userMapByConn.get(connectionsId);
        for(String userString: usersList){
            User user = userMap.get(userString);
            if(current.isBlockingUser(user))
                return null;
            if(!checkBlock(current, user) && !checkBlock(user, current))
                outputLog.add(user.logStatInfo());
        }
        return outputLog;
    }

    public boolean block(int connIDBlocker, String userToBlock){
        if(!userMapByConn.containsKey(connIDBlocker))
            return false;
        User currentUser = userMapByConn.get(connIDBlocker);
        if(! userMap.containsKey(userToBlock) || !currentUser.isloggedIn() || currentUser == userMap.get(userToBlock))
            return false;
        return true;
    }

    public void performBlock(int connIDBlocker, String userToBlock){
        User currentUser = userMapByConn.get(connIDBlocker);
        User blockMe = userMap.get(userToBlock);
        currentUser.unfollowOrFollow(blockMe, false);
        blockMe.unfollowOrFollow(currentUser, false);
        currentUser.addToBlocked(blockMe);
    }

    public boolean checkBlock(User sender, User receiver){
        return receiver.getBlocked().contains(sender);
    }
}

