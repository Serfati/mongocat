package org.bgu.ise.ddb.registration;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bgu.ise.ddb.ParentController;
import org.bgu.ise.ddb.User;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static com.mongodb.client.model.Filters.*;

@RestController
@RequestMapping(value = "/registration")
public class RegistarationController extends ParentController {

    private MongoClient mongoClient = null;

    public MongoDatabase connectToDB() {
        mongoClient = null;
        mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://serfata:1122@sandbox.6lpfa.mongodb.net/admin?w=majority"));
        return mongoClient.getDatabase("adt");
    }

    /**
     * The function checks if the username exist,
     * in case of positive answer HttpStatus in HttpServletResponse should be set to HttpStatus.CONFLICT,
     * else insert the user to the system  and set to HttpStatus in HttpServletResponse HttpStatus.OK
     *
     * @param username  username
     * @param password  password
     * @param firstName firstName
     * @param lastName  lastName
     * @param response  response
     */
    @RequestMapping(value = "register_new_customer", method = {RequestMethod.POST})
    public void registerNewUser(@RequestParam("username") String username,
                                @RequestParam("password") String password,
                                @RequestParam("firstName") String firstName,
                                @RequestParam("lastName") String lastName,
                                HttpServletResponse response) {
        System.out.println(username + " " + password + " " + lastName + " " + firstName);
        try {
            if (isExistUser(username)) {
                HttpStatus status = HttpStatus.CONFLICT;
                response.setStatus(status.value());
            } else {
                MongoDatabase db = connectToDB();
                MongoCollection<Document> dbCollection = db.getCollection("users");
                Document user = new Document();
                user.put("user_name", username);
                user.put("password", password);
                user.put("first_name", firstName);
                user.put("last_name", lastName);
                user.put("date", new Date());
                dbCollection.insertOne(user);
                HttpStatus status = HttpStatus.OK;
                response.setStatus(status.value());
                mongoClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpStatus status = HttpStatus.CONFLICT;
            response.setStatus(status.value());
            mongoClient.close();
        }
    }

    /**
     * The function returns true if the received username exist in the system otherwise false
     *
     * @param username username
     * @return if the received username exist
     * @throws IOException IOException
     */
    @RequestMapping(value = "is_exist_user", method = {RequestMethod.GET})
    public boolean isExistUser(@RequestParam("username") String username) throws IOException {
        System.out.println(username);
        boolean result = false;
        try {
            MongoDatabase db = connectToDB();
            MongoCollection<Document> dbCollection = db.getCollection("users");
            Document user = dbCollection.find(eq("user_name", username)).first();
            if (user != null) result = true;
        } catch (Exception e) {
            e.printStackTrace();
            mongoClient.close();
        }
        return result;
    }

    /**
     * The function returns true if the received username and password match a system storage entry, otherwise false
     *
     * @param username username
     * @return the received username and password match a system storage entry
     * @throws IOException IOException
     */
    @RequestMapping(value = "validate_user", method = {RequestMethod.POST})
    public boolean validateUser(@RequestParam("username") String username,
                                @RequestParam("password") String password) throws IOException {
        System.out.println(username + " " + password);
        boolean result = false;
        try {
            MongoDatabase db = connectToDB();
            MongoCollection<Document> dbCollection = db.getCollection("users");
            Document user = dbCollection.find(and(eq("user_name", username), eq("password", password))).first();
            if (user != null) result = true;
            mongoClient.close();
        } catch (Exception e) {
            e.printStackTrace();
            mongoClient.close();
        }
        return result;
    }

    /**
     * The function retrieves number of the registered users in the past n days
     *
     * @param days days
     * @return return
     * @throws IOException IOException
     */
    @RequestMapping(value = "get_number_of_registred_users", method = {RequestMethod.GET})
    public int getNumberOfRegistredUsers(@RequestParam("days") int days) throws IOException {
        System.out.println(days + "");
        int result = 0;
        MongoDatabase db = connectToDB();
        MongoCollection<Document> dbCollection = db.getCollection("users");
        for (Document ignored :
                dbCollection.find(gte("date", new Date(new Date().getTime() - ((long) days * 24 * 60 * 60 * 1000)))))
            result++;
        mongoClient.close();
        return result;
    }

    /**
     * The function retrieves all the users
     *
     * @return all the users
     */
    @RequestMapping(value = "get_all_users", headers = "Accept=*/*", method = {RequestMethod.GET}, produces = "application/json")
    @ResponseBody
    @org.codehaus.jackson.map.annotate.JsonView(User.class)
    public User[] getAllUsers() {
        MongoDatabase db = connectToDB();
        MongoCollection<Document> dbCollection = db.getCollection("users");
        ArrayList<User> users = new ArrayList<>();
        for (Document cur : dbCollection.find()) {
            String username = cur.getString("user_name");
            String firstName = cur.getString("first_name");
            String lastName = cur.getString("last_name");
            User user = new User(username, firstName, lastName);
            users.add(user);
        }
        User[] usersList = new User[users.size()];
        usersList = users.toArray(usersList);
        mongoClient.close();
        return usersList;
    }
}