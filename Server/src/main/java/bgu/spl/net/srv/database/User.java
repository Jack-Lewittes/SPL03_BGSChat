package bgu.spl.net.srv.database;

import bgu.spl.net.srv.msg.NOTIFICATION;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class User {
    private final String username;
    private final String password;
    private final String birthday;
    private AtomicBoolean isLoggedIn = new AtomicBoolean(false);
    private int age;
    private int numPosts;
    private int connectionHandlerID;
    private LinkedList<User> followers;
    private LinkedList<User> following;
    private LinkedList<User> blocked;
    private ConcurrentLinkedQueue<NOTIFICATION> awaitingMessages;

    public User(String username, String password, String birthday) {
        this.username = username;
        this.password = password;
        this.birthday = birthday;
        this.isLoggedIn.set(false);
        this.numPosts = 0;
        this.age = determineAge(birthday);
        this.followers = new LinkedList<>();
        this.following = new LinkedList<>();
        this.blocked = new LinkedList<>();
        this.awaitingMessages = new ConcurrentLinkedQueue<>();
    }
    public synchronized boolean login(){
        if (isLoggedIn.get())
            return false;
        isLoggedIn.set(true);
        return true;
    }

    public synchronized boolean logout(){
        if (!isLoggedIn.get())
            return false;
        isLoggedIn.set(false);
        return true;
    }

    public synchronized void unfollowOrFollow(User user, boolean doFollow){
        if(doFollow){
            if(!this.isFollowing(user)){
                following.add(user);
                user.addFollower(this);
            }
        }else{
            if(this.isFollowing(user)){
                following.remove(user);
                user.removeFollower(this);
            }
        }
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public int getAge(){return age;}

    public int determineAge(String otherBirthday){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate other = LocalDate.parse(otherBirthday, dtf);
        LocalDate now = LocalDate.now();
        int output;
        int year = now.getYear()-other.getYear();
        int month = now.getMonthValue()-other.getMonthValue();
        if(month < 0)
            output = year + 1;
        else if (month == 0){
            int day = now.getDayOfMonth() - other.getDayOfMonth();
            if(day < 0)
                output = year + 1;
            else {
                output = year;
            }
        }
        else{
            output = year;
        }
        return output;
    }

    public int getNumPosts(){return numPosts;}

    public void upNumPosts(){numPosts++;}

    public boolean isloggedIn() {
        return isLoggedIn.get();
    }

    public boolean isFollowing(User otherUser) {
        return (following.contains(otherUser));
    }

    public void addFollower(User userToAdd){
        this.followers.add(userToAdd);
    }

    public void removeFollower(User userToErase){
        this.followers.remove(userToErase);
    }

    public void addToBlocked(User userToBlock) {
        this.blocked.add(userToBlock);
    }

    public boolean isBlockingUser(User otherUser) {
        return (this.blocked.contains(otherUser));
    }

    public String logStatInfo(){
        return getAge()+" "+getNumPosts()+" "+followers.size()+" "+following.size();
    }

    public LinkedList<User> getFollowers() {
        return followers;
    }

    public LinkedList<User> getFollowing() {
        return following;
    }

    public LinkedList<User> getBlocked() {
        return blocked;
    }

    public ConcurrentLinkedQueue<NOTIFICATION> getAwaitingMessages() {
        return awaitingMessages;
    }

    public int getConnectionHandlerID() {
        return connectionHandlerID;
    }

    public void setConnectionHandlerID(int connectionHandlerID) {
        this.connectionHandlerID = connectionHandlerID;
    }
}
