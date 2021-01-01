package org.bgu.ise.ddb.history;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import org.bgu.ise.ddb.ParentController;
import org.bgu.ise.ddb.User;
import org.bgu.ise.ddb.registration.RegistarationController;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.types.BSONTimestamp;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

@RestController
@RequestMapping(value = "/history")
public class HistoryController extends ParentController {

    private final RegistarationController r = new RegistarationController();
    private MongoClient mongoClient = null;

    public MongoDatabase connectToDB() {
        mongoClient = null;
        mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://serfata:1122@sandbox.6lpfa.mongodb.net/admin?w=majority"));
        return mongoClient.getDatabase("adt");
    }

    /**
     * The function inserts to the system storage triple(s)(username, title, timestamp).
     * The timestamp - in ms since 1970
     * Advice: better to insert the history into two structures( tables) in order to extract it fast one with the key - username, another with the key - title
     *
     * @param username
     * @param title
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "insert_to_history", method = {RequestMethod.GET})
    public void insertToHistory(@RequestParam("username") String username,
                                @RequestParam("title") String title,
                                HttpServletResponse response) throws IOException {
        System.out.println(username + " " + title);
        if (r.isExistUser(username) && isExistTitle(title)) try {
            MongoDatabase db = connectToDB();
            MongoCollection<Document> dbCollection = db.getCollection("history");
            Document hist = new Document();
            hist.put("user_name", username);
            hist.put("title", title);
            hist.put("ts", new BSONTimestamp((int) (new Date().getTime() / 1000), 0));
            dbCollection.insertOne(hist);
            HttpStatus status = HttpStatus.OK;
            response.setStatus(status.value());
            mongoClient.close();
        } catch (Exception e) {
            e.printStackTrace();
            HttpStatus status = HttpStatus.CONFLICT;
            response.setStatus(status.value());
            mongoClient.close();
        }
        else {
            HttpStatus status = HttpStatus.CONFLICT;
            response.setStatus(status.value());
        }
    }

    @RequestMapping(value = "is_exist_item", method = {RequestMethod.GET})
    public boolean isExistTitle(@RequestParam("title") String title) throws IOException {
        boolean result = false;
        try {
            MongoDatabase db = connectToDB();
            MongoCollection<Document> dbCollection = db.getCollection("media_items");
            Document mi = dbCollection.find(eq("title", title)).first();
            assert mi != null;
            if (!mi.isEmpty()) result = true;
        } catch (Exception e) {
            e.printStackTrace();
            mongoClient.close();
        }
        return result;

    }

    /**
     * The function retrieves  users' history
     * The function return array of pairs <title,viewtime> sorted by VIEWTIME in descending order
     *
     * @param username
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "get_history_by_users", headers = "Accept=*/*", method = {RequestMethod.GET}, produces = "application/json")
    @ResponseBody
    @org.codehaus.jackson.map.annotate.JsonView(HistoryPair.class)
    public HistoryPair[] getHistoryByUser(@RequestParam("entity") String username) throws IOException {
        if (r.isExistUser(username)) {
            MongoDatabase db = connectToDB();
            MongoCollection<Document> dbCollection = db.getCollection("history");
            ArrayList<HistoryPair> h = new ArrayList<>();
            for (Document cur : dbCollection.find(eq("user_name", username)).sort(Sorts.descending("ts"))) {
                String title = cur.getString("title");
                getHistory(h, cur, title);
            }
            HistoryPair[] hList = new HistoryPair[h.size()];
            hList = h.toArray(hList);
            mongoClient.close();
            return hList;
        }
        return null;
    }

    private void getHistory(ArrayList<HistoryPair> h, Document cur, String title) {
        BsonTimestamp ts = (BsonTimestamp) cur.get("ts");
        System.out.println(ts);
        Date date = new Date(ts.getTime());
        System.out.println(date);
        HistoryPair hist = new HistoryPair(title, date);
        h.add(hist);
    }

    /**
     * The function retrieves  items' history
     * The function return array of pairs <username,viewtime> sorted by VIEWTIME in descending order
     *
     * @param title
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "get_history_by_items", headers = "Accept=*/*", method = {RequestMethod.GET}, produces = "application/json")
    @ResponseBody
    @org.codehaus.jackson.map.annotate.JsonView(HistoryPair.class)
    public HistoryPair[] getHistoryByItems(@RequestParam("entity") String title) throws IOException {
        if (isExistTitle(title)) {
            MongoDatabase db = connectToDB();
            MongoCollection<Document> dbCollection = db.getCollection("history");
            ArrayList<HistoryPair> h = new ArrayList<>();
            for (Document cur : dbCollection.find(eq("title", title)).sort(Sorts.descending("ts"))) {
                String username = cur.getString("user_name");
                getHistory(h, cur, username);
            }
            HistoryPair[] hList = new HistoryPair[h.size()];
            hList = h.toArray(hList);
            mongoClient.close();
            return hList;
        }
        return null;
    }

    /**
     * The function retrieves all the  users that have viewed the given item
     *
     * @param title
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "get_users_by_item", headers = "Accept=*/*", method = {RequestMethod.GET}, produces = "application/json")
    @ResponseBody
    @org.codehaus.jackson.map.annotate.JsonView(HistoryPair.class)
    public User[] getUsersByItem(@RequestParam("title") String title) throws IOException {
        ArrayList<User> users = new ArrayList<>();
        if (isExistTitle(title)) {
            MongoDatabase db = connectToDB();
            MongoCollection<Document> dbCollection = db.getCollection("history");
            for (Document cur : dbCollection.find(eq("title", title)).sort(Sorts.descending("ts"))) {
                String username = cur.getString("user_name");
                MongoCollection<Document> userdbCollection = db.getCollection("users");
                Document user = userdbCollection.find(eq("user_name", username)).first();
                assert user != null;
                String name = user.getString("user_name");
                String firstName = user.getString("first_name");
                String lastName = user.getString("last_name");
                User userView = new User(name, firstName, lastName);
                users.add(userView);
            }
            User[] userViews = new User[users.size()];
            userViews = users.toArray(userViews);
            mongoClient.close();
            return userViews;
        }
        return null;
    }

    /**
     * The function calculates the similarity score using Jaccard similarity function:
     * sim(i,j) = |U(i) intersection U(j)|/|U(i) union U(j)|,
     * where U(i) is the set of usernames which exist in the history of the item i.
     *
     * @param title1
     * @param title2
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "get_items_similarity", headers = "Accept=*/*", method = {RequestMethod.GET}, produces = "application/json")
    @ResponseBody
    public double getItemsSimilarity(@RequestParam("title1") String title1,
                                     @RequestParam("title2") String title2) throws IOException {
        double similarity = 0.0;
        Set<String> user1 = usernameList(getHistoryByItems(title1));
        Set<String> user2 = usernameList(getHistoryByItems(title2));
        Set<String> unionList = new HashSet<>(user1);
        unionList.addAll(user2);
        Set<String> intersectionList = new HashSet<>(user1);
        intersectionList.retainAll(user2);
        if (unionList.size() == 0) return similarity;
        similarity = ((double) intersectionList.size()) / unionList.size();
        return similarity;
    }

    private Set<String> usernameList(HistoryPair[] hList) {
        return Arrays.stream(hList).map(historyPair -> historyPair.credentials).collect(Collectors.toSet());
    }


}